package com.group34.eval_suite.runs.dto;

import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings({"PMD.ShortVariable", "PMD.LongVariable"})
public record RunResponse(
    UUID id,
    UUID systemPromptId,
    UUID systemPromptFamilyId,
    String systemPromptName,
    Integer systemPromptVersionNumber,
    boolean promptVersionActive,
    UUID datasetId,
    String datasetName,
    OffsetDateTime createdAt,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    OffsetDateTime updatedAt,
    RunStatus status,
    Integer outputScore,
    Integer systemPromptScore,
    String systemPromptFeedback,
    String systemPromptImprovementSuggestion,
    boolean automaticImprovementEnabled,
    Integer automaticImprovementAttempt,
    String failureMessage) {

  public static RunResponse fromEntity(
      final Run entity,
      final UUID systemPromptFamilyId,
      final String systemPromptName,
      final Integer systemPromptVersionNumber,
      final boolean promptVersionActive,
      final String datasetName) {
    return new RunResponse(
        entity.getId(),
        entity.getSystemPromptId(),
        systemPromptFamilyId,
        systemPromptName,
        systemPromptVersionNumber,
        promptVersionActive,
        entity.getDatasetId(),
        datasetName,
        entity.getCreatedAt(),
        entity.getStartedAt(),
        entity.getCompletedAt(),
        entity.getUpdatedAt(),
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
