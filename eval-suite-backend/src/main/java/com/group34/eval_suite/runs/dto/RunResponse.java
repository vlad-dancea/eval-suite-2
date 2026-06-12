package com.group34.eval_suite.runs.dto;

import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.enums.RunStatus;
import com.group34.eval_suite.runs.records.RunItemResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.LongVariable", "PMD.ShortVariable"})
public record RunResponse(
    UUID id,
    UUID systemPromptId,
    UUID systemPromptFamilyId,
    int systemPromptVersionNumber,
    String systemPromptName,
    String systemPromptContent,
    UUID datasetId,
    String datasetName,
    int datasetItemCount,
    RunStatus status,
    BigDecimal averageOutputScore,
    BigDecimal averagePromptScore,
    String errorMessage,
    OffsetDateTime createdAt,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    OffsetDateTime deletedAt,
    List<RunItemResponse> items) {

  public RunResponse {
    items = items == null ? List.of() : List.copyOf(items);
  }

  @Override
  public List<RunItemResponse> items() {
    return List.copyOf(items);
  }

  public static RunResponse fromEntity(Run entity) {
    final List<RunItemResponse> items =
        entity.getItems().stream().map(RunItemResponse::fromEntity).toList();
    return new RunResponse(
        entity.getId(),
        entity.getSystemPromptId(),
        entity.getSystemPromptFamilyId(),
        entity.getSystemPromptVersionNumber(),
        entity.getSystemPromptName(),
        entity.getSystemPromptContent(),
        entity.getDatasetId(),
        entity.getDatasetName(),
        entity.getDatasetItemCount(),
        entity.getStatus(),
        entity.getAverageOutputScore(),
        entity.getAveragePromptScore(),
        entity.getErrorMessage(),
        entity.getCreatedAt(),
        entity.getStartedAt(),
        entity.getCompletedAt(),
        entity.getDeletedAt(),
        items);
  }
}
