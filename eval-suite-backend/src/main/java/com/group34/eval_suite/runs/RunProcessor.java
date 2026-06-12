package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.dto.RunSummaryResponse;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunItem;
import com.group34.eval_suite.runs.enums.RunStatus;
import com.group34.eval_suite.runs.records.JudgeResult;
import com.group34.eval_suite.runs.records.RunEvent;
import com.group34.eval_suite.runs.repo.RunRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@SuppressWarnings({
  "PMD.AvoidCatchingGenericException",
  "PMD.GuardLogStatement",
  // The orElseThrow exception supplier in the item loop only allocates when the item is missing.
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public class RunProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(RunProcessor.class);

  private static final String RUN_UPDATED = "run.updated";
  private static final int ONE_ERROR = 1;

  private final RunRepository runRepository;
  private final AqueductLlmClient aqueductLlmClient;
  private final RunEventPublisher runEventPublisher;
  private final TransactionTemplate txTemplate;

  public RunProcessor(
      RunRepository runRepository,
      AqueductLlmClient aqueductLlmClient,
      RunEventPublisher runEventPublisher,
      TransactionTemplate txTemplate) {
    this.runRepository = runRepository;
    this.aqueductLlmClient = aqueductLlmClient;
    this.runEventPublisher = runEventPublisher;
    this.txTemplate = txTemplate;
  }

  @Async
  public void processAsync(UUID runId) {
    LOG.info("Run {}: starting processing", runId);
    try {
      process(runId);
    } catch (RuntimeException ex) {
      LOG.error("Run {}: processing aborted unexpectedly", runId, ex);
      failRun(runId, ex);
    }
  }

  private void process(UUID runId) {
    final Run started =
        updateRun(
            runId,
            run -> {
              run.setStatus(RunStatus.RUNNING);
              run.setStartedAt(OffsetDateTime.now());
            });
    publish(RUN_UPDATED, started);

    for (final RunItem item : started.getItems()) {
      final Run updated =
          updateRun(
              runId,
              run -> {
                final RunItem currentItem =
                    run.getItems().stream()
                        .filter(candidate -> candidate.getId().equals(item.getId()))
                        .findFirst()
                        .orElseThrow(
                            () ->
                                new IllegalStateException(
                                    "Run item " + item.getId() + " disappeared during processing"));
                processItem(run, currentItem);
              });
      publish(RUN_UPDATED, updated);
    }

    final Run completed = updateRun(runId, this::completeRun);
    LOG.info(
        "Run {}: finished with status {}{}",
        runId,
        completed.getStatus(),
        completed.getErrorMessage() == null ? "" : " — " + completed.getErrorMessage());
    publish(RUN_UPDATED, completed);
  }

  private void failRun(UUID runId, RuntimeException cause) {
    try {
      final Run failed =
          updateRun(
              runId,
              run -> {
                run.setStatus(RunStatus.FAILED);
                run.setErrorMessage(cause.getMessage());
                run.setCompletedAt(OffsetDateTime.now());
              });
      publish(RUN_UPDATED, failed);
    } catch (RuntimeException ex) {
      LOG.error("Run {}: could not mark run as failed", runId, ex);
    }
  }

  private void processItem(Run run, RunItem item) {
    LOG.info(
        "Run {}: processing item {} (position {})", run.getId(), item.getId(), item.getPosition());
    item.setStatus(RunStatus.RUNNING);
    try {
      final String modelOutput =
          aqueductLlmClient.generateOutput(run.getSystemPromptContent(), item.getInput());
      final JudgeResult judgeResult =
          aqueductLlmClient.judgeOutput(
              run.getSystemPromptContent(), item.getInput(), modelOutput, item.getExpectedOutput());

      item.setModelOutput(modelOutput);
      item.setOutputScore(judgeResult.outputScore());
      item.setPromptScore(judgeResult.promptScore());
      item.setJudgeExplanation(judgeResult.explanation());
      item.setImprovementSuggestion(judgeResult.improvementSuggestion());
      item.setStatus(RunStatus.SUCCEEDED);
    } catch (RuntimeException ex) {
      LOG.error("Run {}: item {} failed", run.getId(), item.getId(), ex);
      item.setStatus(RunStatus.FAILED);
      item.setErrorMessage(ex.getMessage());
    } finally {
      item.setCompletedAt(OffsetDateTime.now());
    }
  }

  private void completeRun(Run run) {
    final List<RunItem> succeeded =
        run.getItems().stream().filter(item -> item.getStatus() == RunStatus.SUCCEEDED).toList();
    if (!succeeded.isEmpty()) {
      run.setAverageOutputScore(averageScore(succeeded, RunItem::getOutputScore));
      run.setAveragePromptScore(averageScore(succeeded, RunItem::getPromptScore));
    }

    final boolean hasFailedItem =
        run.getItems().stream().anyMatch(item -> item.getStatus() == RunStatus.FAILED);
    run.setStatus(hasFailedItem ? RunStatus.FAILED : RunStatus.SUCCEEDED);
    if (hasFailedItem) {
      run.setErrorMessage(runErrorMessage(run));
    }
    run.setCompletedAt(OffsetDateTime.now());
  }

  private Run updateRun(UUID runId, RunUpdater updater) {
    return txTemplate.execute(
        status -> {
          final Run run = runRepository.findByIdWithItems(runId).orElseThrow();
          updater.update(run);
          return runRepository.saveAndFlush(run);
        });
  }

  private static String runErrorMessage(Run run) {
    final List<String> itemErrors =
        run.getItems().stream()
            .filter(item -> item.getStatus() == RunStatus.FAILED)
            .map(RunItem::getErrorMessage)
            .filter(error -> error != null && !error.isBlank())
            .distinct()
            .toList();
    if (itemErrors.size() == ONE_ERROR) {
      return itemErrors.getFirst();
    }
    return "One or more dataset items failed while processing this run.";
  }

  private static BigDecimal averageScore(List<RunItem> items, Function<RunItem, Integer> score) {
    final double average =
        items.stream()
            .mapToInt(
                item -> {
                  final Integer value = score.apply(item);
                  return value == null ? 0 : value;
                })
            .average()
            .orElse(0);
    return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
  }

  private void publish(String type, Run run) {
    runEventPublisher.publish(new RunEvent(type, run.getId(), RunSummaryResponse.fromEntity(run)));
  }

  @FunctionalInterface
  private interface RunUpdater {
    void update(Run run);
  }
}
