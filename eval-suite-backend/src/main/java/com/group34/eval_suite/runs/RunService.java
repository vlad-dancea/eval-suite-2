package com.group34.eval_suite.runs;

import com.group34.eval_suite.datasets.Dataset;
import com.group34.eval_suite.datasets.DatasetItem;
import com.group34.eval_suite.datasets.DatasetRepository;
import com.group34.eval_suite.runs.dto.RunResponse;
import com.group34.eval_suite.runs.dto.RunSummaryResponse;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunItem;
import com.group34.eval_suite.runs.enums.RunStatus;
import com.group34.eval_suite.runs.records.RunEvent;
import com.group34.eval_suite.runs.repo.RunRepository;
import com.group34.eval_suite.systemprompts.SystemPrompt;
import com.group34.eval_suite.systemprompts.SystemPromptRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
@SuppressWarnings({"PMD.ShortVariable", "PMD.LongVariable", "PMD.GuardLogStatement"})
public class RunService {

  private static final Logger LOG = LoggerFactory.getLogger(RunService.class);

  private final RunRepository runRepository;
  private final SystemPromptRepository systemPromptRepository;
  private final DatasetRepository datasetRepository;
  private final RunProcessor runProcessor;
  private final RunEventPublisher runEventPublisher;

  public RunService(
      RunRepository runRepository,
      SystemPromptRepository systemPromptRepository,
      DatasetRepository datasetRepository,
      RunProcessor runProcessor,
      RunEventPublisher runEventPublisher) {
    this.runRepository = runRepository;
    this.systemPromptRepository = systemPromptRepository;
    this.datasetRepository = datasetRepository;
    this.runProcessor = runProcessor;
    this.runEventPublisher = runEventPublisher;
  }

  @Transactional
  public RunResponse createRun(UUID systemPromptId, UUID datasetId) {
    final SystemPrompt prompt = findActivePrompt(systemPromptId);
    final Dataset dataset =
        datasetRepository
            .findActiveByIdWithItems(datasetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));

    final Run run =
        Run.builder()
            .id(UUID.randomUUID())
            .systemPromptId(prompt.getId())
            .systemPromptFamilyId(prompt.getFamilyId())
            .systemPromptVersionNumber(prompt.getVersionNumber())
            .systemPromptName(prompt.getName())
            .systemPromptContent(prompt.getContent())
            .datasetId(dataset.getId())
            .datasetName(dataset.getName())
            .datasetItemCount(dataset.getItems().size())
            .status(RunStatus.QUEUED)
            .createdAt(OffsetDateTime.now())
            .build();

    for (final DatasetItem datasetItem : dataset.getItems()) {
      run.getItems().add(buildRunItem(run, datasetItem));
    }

    final Run saved = runRepository.save(run);
    LOG.info(
        "Run {}: created for prompt '{}' and dataset '{}' ({} items)",
        saved.getId(),
        saved.getSystemPromptName(),
        saved.getDatasetName(),
        saved.getDatasetItemCount());
    publish("run.created", saved);
    final UUID savedId = saved.getId();
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            runProcessor.processAsync(savedId);
          }
        });
    return RunResponse.fromEntity(saved);
  }

  public List<RunSummaryResponse> getActiveRuns() {
    return runRepository.findByDeletedAtIsNullOrderByCreatedAtDesc().stream()
        .map(RunSummaryResponse::fromEntity)
        .toList();
  }

  @Transactional(readOnly = true)
  public RunResponse getRun(UUID id) {
    return RunResponse.fromEntity(
        runRepository
            .findActiveByIdWithItems(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found")));
  }

  @Transactional
  public void deleteRun(UUID id) {
    final Run run =
        runRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    if (run.getDeletedAt() == null) {
      run.setDeletedAt(OffsetDateTime.now());
      publish("run.deleted", run);
    }
  }

  private SystemPrompt findActivePrompt(UUID id) {
    final SystemPrompt prompt =
        systemPromptRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "System prompt not found"));
    if (prompt.getDeletedAt() != null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "System prompt not found");
    }
    return prompt;
  }

  private static RunItem buildRunItem(Run run, DatasetItem datasetItem) {
    return RunItem.builder()
        .id(UUID.randomUUID())
        .run(run)
        .datasetItemId(datasetItem.getId())
        .position(datasetItem.getPosition())
        .input(datasetItem.getInput())
        .expectedOutput(datasetItem.getExpectedOutput())
        .status(RunStatus.QUEUED)
        .createdAt(OffsetDateTime.now())
        .build();
  }

  private void publish(String type, Run run) {
    runEventPublisher.publish(new RunEvent(type, run.getId(), RunSummaryResponse.fromEntity(run)));
  }
}
