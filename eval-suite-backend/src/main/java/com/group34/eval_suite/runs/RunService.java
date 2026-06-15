package com.group34.eval_suite.runs;

import com.group34.eval_suite.datasets.Dataset;
import com.group34.eval_suite.datasets.DatasetRepository;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunStatus;
import com.group34.eval_suite.systemprompts.SystemPrompt;
import com.group34.eval_suite.systemprompts.SystemPromptRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.LongVariable")
public class RunService {

  private final RunRepository runRepository;
  private final SystemPromptRepository systemPromptRepository;
  private final DatasetRepository datasetRepository;

  @Transactional
  public Run createRun(
      final UUID systemPromptId, final UUID datasetId, final boolean automaticImprovementEnabled) {
    validateActiveSystemPrompt(systemPromptId);
    validateActiveDataset(datasetId);

    final Run run =
        Run.builder()
            .id(UUID.randomUUID())
            .systemPromptId(systemPromptId)
            .datasetId(datasetId)
            .createdAt(OffsetDateTime.now())
            .status(RunStatus.QUEUED)
            .automaticImprovementEnabled(automaticImprovementEnabled)
            .automaticImprovementAttempt(0)
            .build();

    return runRepository.save(run);
  }

  @Transactional(readOnly = true)
  public List<Run> getRuns() {
    return runRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
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
