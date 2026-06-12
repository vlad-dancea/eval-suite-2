package com.group34.eval_suite.runs.dto;

import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.enums.RunStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings({"PMD.LongVariable", "PMD.ShortVariable"})
public record RunSummaryResponse(
    UUID id,
    String systemPromptName,
    int systemPromptVersionNumber,
    String datasetName,
    int datasetItemCount,
    RunStatus status,
    BigDecimal averageOutputScore,
    BigDecimal averagePromptScore,
    String errorMessage,
    OffsetDateTime createdAt,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {

  public static RunSummaryResponse fromEntity(Run entity) {
    return new RunSummaryResponse(
        entity.getId(),
        entity.getSystemPromptName(),
        entity.getSystemPromptVersionNumber(),
        entity.getDatasetName(),
        entity.getDatasetItemCount(),
        entity.getStatus(),
        entity.getAverageOutputScore(),
        entity.getAveragePromptScore(),
        entity.getErrorMessage(),
        entity.getCreatedAt(),
        entity.getStartedAt(),
        entity.getCompletedAt());
  }
}
