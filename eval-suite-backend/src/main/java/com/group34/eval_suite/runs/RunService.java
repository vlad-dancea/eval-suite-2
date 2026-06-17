package com.group34.eval_suite.runs;

import com.group34.eval_suite.datasets.Dataset;
import com.group34.eval_suite.datasets.DatasetItem;
import com.group34.eval_suite.datasets.DatasetItemRepository;
import com.group34.eval_suite.datasets.DatasetRepository;
import com.group34.eval_suite.runs.dto.RunDetailResponse;
import com.group34.eval_suite.runs.dto.RunDetailResponse.RunItemResultResponse;
import com.group34.eval_suite.runs.dto.RunResponse;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunDatasetItemResult;
import com.group34.eval_suite.runs.entity.RunStatus;
import com.group34.eval_suite.systemprompts.SystemPrompt;
import com.group34.eval_suite.systemprompts.SystemPromptRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LongVariable", "PMD.TooManyMethods"})
public class RunService {

  private final RunRepository runRepository;
  private final SystemPromptRepository systemPromptRepository;
  private final DatasetRepository datasetRepository;
  private final DatasetItemRepository datasetItemRepository;

  @Transactional
  public Run createRun(
      final UUID systemPromptId, final UUID datasetId, final boolean automaticImprovementEnabled) {
    validateActiveSystemPrompt(systemPromptId);
    validateActiveDataset(datasetId);

    final OffsetDateTime now = OffsetDateTime.now();
    final Run run =
        Run.builder()
            .id(UUID.randomUUID())
            .systemPromptId(systemPromptId)
            .datasetId(datasetId)
            .createdAt(now)
            .updatedAt(now)
            .status(RunStatus.QUEUED)
            .automaticImprovementEnabled(automaticImprovementEnabled)
            .automaticImprovementAttempt(0)
            .build();

    return runRepository.save(run);
  }

  /**
   * Create a queued child run for an automatic-improvement attempt. The child never drives its own
   * improvement loop ({@code automaticImprovementEnabled = false}); the root run's background task
   * is responsible for the whole loop.
   */
  @Transactional
  public Run createChildRun(final UUID systemPromptId, final UUID datasetId, final int attempt) {
    final OffsetDateTime now = OffsetDateTime.now();
    final Run run =
        Run.builder()
            .id(UUID.randomUUID())
            .systemPromptId(systemPromptId)
            .datasetId(datasetId)
            .createdAt(now)
            .updatedAt(now)
            .status(RunStatus.QUEUED)
            .automaticImprovementEnabled(false)
            .automaticImprovementAttempt(attempt)
            .build();

    return runRepository.save(run);
  }

  @Transactional
  public Run markRunning(final UUID runId) {
    final Run run = requireRun(runId);
    final OffsetDateTime now = OffsetDateTime.now();
    run.setStatus(RunStatus.RUNNING);
    run.setStartedAt(now);
    run.setUpdatedAt(now);
    return runRepository.save(run);
  }

  @Transactional
  public Run completeRun(
      final UUID runId,
      final Integer outputScore,
      final List<RunDatasetItemResult> itemResults,
      final Integer systemPromptScore,
      final String systemPromptFeedback,
      final String systemPromptImprovementSuggestion) {
    final Run run = requireRun(runId);
    final OffsetDateTime now = OffsetDateTime.now();
    run.setOutputScore(outputScore);
    run.setSystemPromptScore(systemPromptScore);
    run.setSystemPromptFeedback(systemPromptFeedback);
    run.setSystemPromptImprovementSuggestion(systemPromptImprovementSuggestion);
    run.getItemResults().clear();
    run.getItemResults().addAll(itemResults);
    run.setStatus(RunStatus.SUCCEEDED);
    run.setCompletedAt(now);
    run.setUpdatedAt(now);
    return runRepository.save(run);
  }

  @Transactional
  public Run failRun(final UUID runId, final String failureMessage) {
    final Run run = requireRun(runId);
    final OffsetDateTime now = OffsetDateTime.now();
    run.setStatus(RunStatus.FAILED);
    run.setFailureMessage(failureMessage);
    run.setCompletedAt(now);
    run.setUpdatedAt(now);
    return runRepository.save(run);
  }

  @Transactional(readOnly = true)
  public List<RunResponse> getRuns() {
    final List<Run> runs = runRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    return toResponses(runs);
  }

  @Transactional(readOnly = true)
  public RunResponse getRunResponse(final UUID runId) {
    return toResponse(requireRun(runId));
  }

  @Transactional(readOnly = true)
  public RunDetailResponse getRunDetail(final UUID runId) {
    final Run run = requireRun(runId);
    final List<RunDatasetItemResult> results = run.getItemResults();

    final Map<UUID, DatasetItem> itemsById =
        datasetItemRepository
            .findAllById(results.stream().map(RunDatasetItemResult::getDatasetItemId).toList())
            .stream()
            .collect(Collectors.toMap(DatasetItem::getId, Function.identity()));

    final List<RunItemResultResponse> items =
        results.stream()
            .map(
                result -> {
                  final DatasetItem item = itemsById.get(result.getDatasetItemId());
                  return new RunItemResultResponse(
                      result.getDatasetItemId(),
                      result.getPosition(),
                      item == null ? null : item.getInput(),
                      item == null ? null : item.getExpectedOutput(),
                      result.getModelOutput(),
                      result.getOutputScore(),
                      result.getJudgeFeedback());
                })
            .toList();

    return new RunDetailResponse(toResponse(run), items);
  }

  @Transactional(readOnly = true)
  public RunResponse toResponse(final Run run) {
    final SystemPrompt systemPrompt =
        systemPromptRepository.findById(run.getSystemPromptId()).orElse(null);
    final String datasetName =
        datasetRepository
            .findById(run.getDatasetId())
            .map(Dataset::getName)
            .orElse("Unknown dataset");

    return RunResponse.fromEntity(
        run,
        systemPrompt == null ? null : systemPrompt.getFamilyId(),
        systemPrompt == null ? "Unknown prompt" : systemPrompt.getName(),
        systemPrompt == null ? null : systemPrompt.getVersionNumber(),
        systemPrompt != null && systemPrompt.getDeletedAt() == null,
        datasetName);
  }

  private List<RunResponse> toResponses(final List<Run> runs) {
    final Map<UUID, SystemPrompt> promptsById =
        systemPromptRepository
            .findAllById(runs.stream().map(Run::getSystemPromptId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(SystemPrompt::getId, Function.identity()));
    final Map<UUID, Dataset> datasetsById =
        datasetRepository
            .findAllById(runs.stream().map(Run::getDatasetId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Dataset::getId, Function.identity()));

    return runs.stream()
        .map(
            run -> {
              final SystemPrompt prompt = promptsById.get(run.getSystemPromptId());
              final Dataset dataset = datasetsById.get(run.getDatasetId());
              return RunResponse.fromEntity(
                  run,
                  prompt == null ? null : prompt.getFamilyId(),
                  prompt == null ? "Unknown prompt" : prompt.getName(),
                  prompt == null ? null : prompt.getVersionNumber(),
                  prompt != null && prompt.getDeletedAt() == null,
                  dataset == null ? "Unknown dataset" : dataset.getName());
            })
        .toList();
  }

  private Run requireRun(final UUID runId) {
    return runRepository
        .findById(runId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
  }

  private void validateActiveSystemPrompt(final UUID systemPromptId) {
    final SystemPrompt systemPrompt =
        systemPromptRepository
            .findById(systemPromptId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "System prompt not found"));

    if (systemPrompt.getDeletedAt() != null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "System prompt not found");
    }
  }

  private void validateActiveDataset(final UUID datasetId) {
    final Dataset dataset =
        datasetRepository
            .findActiveByIdWithItems(datasetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

    if (dataset.getItems().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dataset has no items");
    }
  }
}
