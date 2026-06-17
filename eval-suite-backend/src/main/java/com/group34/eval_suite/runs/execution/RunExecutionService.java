package com.group34.eval_suite.runs.execution;

import com.group34.eval_suite.ai.openai.client.OpenAiClient;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionRequest;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionResponse;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatMessage;
import com.group34.eval_suite.datasets.Dataset;
import com.group34.eval_suite.datasets.DatasetItem;
import com.group34.eval_suite.datasets.DatasetRepository;
import com.group34.eval_suite.runs.RunRepository;
import com.group34.eval_suite.runs.RunService;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunDatasetItemResult;
import com.group34.eval_suite.runs.events.RunEventService;
import com.group34.eval_suite.systemprompts.SystemPrompt;
import com.group34.eval_suite.systemprompts.SystemPromptRepository;
import com.group34.eval_suite.systemprompts.SystemPromptService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs the evaluation pipeline for a run on a background thread: generate model output per dataset
 * item, grade each output, grade the system prompt, and — when automatic improvement is enabled —
 * loop producing improved prompt versions while the prompt grade keeps improving.
 *
 * <p>LLM calls happen outside transactions; all database writes go through {@link RunService}'s
 * short transactional methods (a separate bean, so the {@code @Transactional} proxy applies).
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LongVariable", "PMD.GuardLogStatement"})
public class RunExecutionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RunExecutionService.class);

  private static final double TEMPERATURE = 0.0;
  private static final int MAX_TOKENS = 4096;
  private static final String FINISH_REASON_LENGTH = "length";
  private static final int MAX_PROMPT_CHARS = 5000;
  private static final int MAX_IMPROVEMENT_ATTEMPTS = 10;

  private final OpenAiClient openAiClient;
  private final RunService runService;
  private final SystemPromptService systemPromptService;
  private final RunRepository runRepository;
  private final SystemPromptRepository systemPromptRepository;
  private final DatasetRepository datasetRepository;
  private final RunEventService eventService;
  private final RunPromptFactory promptFactory;
  private final JudgeGradeParser judgeGradeParser;

  @Async("runExecutor")
  public void executeAsync(final UUID runId) {
    try {
      final Run run =
          runRepository
              .findById(runId)
              .orElseThrow(() -> new RunExecutionException("Run not found: " + runId));
      if (run.isAutomaticImprovementEnabled()) {
        runWithImprovement(run);
      } else {
        runEvaluation(run);
      }
    } catch (final RunExecutionException exception) {
      // The failing run was already marked FAILED and published inside runEvaluation.
      LOGGER.warn("Run execution stopped: {}", exception.getMessage());
    } catch (final RuntimeException exception) {
      LOGGER.error("Unexpected error executing run {}", runId, exception);
      failRunQuietly(runId, exception.getMessage());
    }
  }

  private void runWithImprovement(final Run rootRun) {
    final EvaluationResult base = runEvaluation(rootRun);

    final SystemPrompt rootPrompt =
        systemPromptRepository
            .findById(rootRun.getSystemPromptId())
            .orElseThrow(() -> new RunExecutionException("System prompt not found"));
    final UUID familyId = rootPrompt.getFamilyId();
    final String name = rootPrompt.getName();
    final UUID datasetId = rootRun.getDatasetId();

    int bestScore = base.systemPromptScore();
    String bestContent = base.promptContent();
    List<EvaluatedItem> bestItems = base.items();

    for (int attempt = 1; attempt <= MAX_IMPROVEMENT_ATTEMPTS; attempt++) {
      try {
        final String improved =
            sanitizePromptContent(callLlm(promptFactory.improvement(bestContent, bestItems)));
        if (improved.isBlank()) {
          break;
        }

        final SystemPrompt newVersion = systemPromptService.createPrompt(name, improved, familyId);
        final Run child = runService.createChildRun(newVersion.getId(), datasetId, attempt);
        eventService.publishRunCreated(runService.getRunResponse(child.getId()));

        final EvaluationResult childEval = runEvaluation(child);
        if (childEval.systemPromptScore() > bestScore) {
          bestScore = childEval.systemPromptScore();
          bestContent = childEval.promptContent();
          bestItems = childEval.items();
        } else {
          // No improvement: discard the new version (the previous best becomes active again) and
          // stop. The child run stays for the family timeline, now flagged as an inactive version.
          systemPromptService.discardVersion(newVersion.getId());
          eventService.publishRunUpdated(runService.getRunResponse(child.getId()));
          break;
        }
      } catch (final RunExecutionException childFailure) {
        // The child run is already marked FAILED and published; stop improving.
        LOGGER.warn("Improvement attempt {} failed: {}", attempt, childFailure.getMessage());
        break;
      }
    }
  }

  private EvaluationResult runEvaluation(final Run run) {
    try {
      eventService.publishRunUpdated(runService.toResponse(runService.markRunning(run.getId())));

      final SystemPrompt prompt =
          systemPromptRepository
              .findById(run.getSystemPromptId())
              .orElseThrow(() -> new RunExecutionException("System prompt not found"));
      final Dataset dataset =
          datasetRepository
              .findActiveByIdWithItems(run.getDatasetId())
              .orElseThrow(() -> new RunExecutionException("Dataset not found"));
      final List<DatasetItem> items = dataset.getItems();
      if (items.isEmpty()) {
        throw new RunExecutionException("Dataset has no items");
      }

      final List<RunDatasetItemResult> results = new ArrayList<>(items.size());
      final List<EvaluatedItem> evaluated = new ArrayList<>(items.size());
      for (final DatasetItem item : items) {
        final String output =
            callLlm(promptFactory.generation(prompt.getContent(), item.getInput()));
        final JudgeGrade grade =
            judgeGradeParser.parse(
                callLlm(
                    promptFactory.outputGrading(
                        item.getInput(), item.getExpectedOutput(), output)));

        results.add(
            RunDatasetItemResult.builder()
                .datasetItemId(item.getId())
                .position(item.getPosition())
                .modelOutput(output)
                .outputScore(grade.score())
                .judgeFeedback(grade.feedback())
                .build());
        evaluated.add(
            new EvaluatedItem(
                item.getInput(), item.getExpectedOutput(), output, grade.score()));
      }

      final Integer outputScore = aggregateScore(results);
      final JudgeGrade promptGrade =
          judgeGradeParser.parse(
              callLlm(promptFactory.promptGrading(prompt.getContent(), evaluated)));

      final Run completed =
          runService.completeRun(
              run.getId(),
              outputScore,
              results,
              promptGrade.score(),
              promptGrade.feedback(),
              promptGrade.suggestion());
      eventService.publishRunUpdated(runService.toResponse(completed));

      return new EvaluationResult(promptGrade.score(), prompt.getContent(), evaluated);
    } catch (final RuntimeException exception) {
      failRunQuietly(run.getId(), exception.getMessage());
      throw new RunExecutionException(
          "Run " + run.getId() + " failed: " + exception.getMessage(), exception);
    }
  }

  private String callLlm(final List<OpenAiChatMessage> messages) {
    final OpenAiChatCompletionResponse response =
        openAiClient.createChatCompletion(
            new OpenAiChatCompletionRequest(messages, TEMPERATURE, MAX_TOKENS));
    if (response.choices().isEmpty()) {
      throw new RunExecutionException("LLM returned no choices");
    }
    final var choice = response.choices().getFirst();
    final String content = choice.message().content();
    if (content == null || content.isBlank()) {
      throw new RunExecutionException("LLM returned empty content");
    }
    if (FINISH_REASON_LENGTH.equals(choice.finishReason())) {
      throw new RunExecutionException(
          "LLM response was truncated at the " + MAX_TOKENS + "-token limit: " + content);
    }
    return content;
  }

  private Integer aggregateScore(final List<RunDatasetItemResult> results) {
    final List<Integer> scores =
        results.stream().map(RunDatasetItemResult::getOutputScore).filter(score -> score != null)
            .toList();
    if (scores.isEmpty()) {
      return null;
    }
    final double average = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    return (int) Math.round(average);
  }

  private String sanitizePromptContent(final String raw) {
    String text = raw.strip();
    if (text.startsWith("```")) {
      final int firstNewline = text.indexOf('\n');
      if (firstNewline >= 0) {
        text = text.substring(firstNewline + 1);
      }
      if (text.endsWith("```")) {
        text = text.substring(0, text.length() - 3);
      }
      text = text.strip();
    }
    if (text.length() > MAX_PROMPT_CHARS) {
      text = text.substring(0, MAX_PROMPT_CHARS);
    }
    return text;
  }

  private void failRunQuietly(final UUID runId, final String message) {
    try {
      runService.failRun(runId, message);
      eventService.publishRunFailed(runService.getRunResponse(runId));
    } catch (final RuntimeException exception) {
      LOGGER.error("Failed to mark run {} as failed", runId, exception);
    }
  }
}
