package com.group34.eval_suite.runs.dto;

import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings({"PMD.ShortVariable", "PMD.LongVariable"})
public record RunResponse(
    UUID id,
    UUID systemPromptId,
    UUID datasetId,
    OffsetDateTime createdAt,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    RunStatus status,
    Integer outputScore,
    Integer systemPromptScore,
    String systemPromptFeedback,
    String systemPromptImprovementSuggestion,
    boolean automaticImprovementEnabled,
    Integer automaticImprovementAttempt,
    String failureMessage) {

  public static RunResponse fromEntity(final Run entity) {
    return new RunResponse(
        entity.getId(),
        entity.getSystemPromptId(),
        entity.getDatasetId(),
        entity.getCreatedAt(),
        entity.getStartedAt(),
        entity.getCompletedAt(),
        entity.getStatus(),
        entity.getOutputScore(),
        entity.getSystemPromptScore(),
        entity.getSystemPromptFeedback(),
        entity.getSystemPromptImprovementSuggestion(),
        entity.isAutomaticImprovementEnabled(),
        entity.getAutomaticImprovementAttempt(),
        entity.getFailureMessage());
  }
}
