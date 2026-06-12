package com.group34.eval_suite.runs.records;

import com.group34.eval_suite.runs.entity.RunItem;
import com.group34.eval_suite.runs.enums.RunStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings({"PMD.LongVariable", "PMD.ShortVariable"})
public record RunItemResponse(
    UUID id,
    UUID datasetItemId,
    int position,
    String input,
    String expectedOutput,
    String modelOutput,
    Integer outputScore,
    Integer promptScore,
    String judgeExplanation,
    String improvementSuggestion,
    RunStatus status,
    String errorMessage,
    OffsetDateTime createdAt,
    OffsetDateTime completedAt) {

  public static RunItemResponse fromEntity(RunItem entity) {
    return new RunItemResponse(
        entity.getId(),
        entity.getDatasetItemId(),
        entity.getPosition(),
        entity.getInput(),
        entity.getExpectedOutput(),
        entity.getModelOutput(),
        entity.getOutputScore(),
        entity.getPromptScore(),
        entity.getJudgeExplanation(),
        entity.getImprovementSuggestion(),
        entity.getStatus(),
        entity.getErrorMessage(),
        entity.getCreatedAt(),
        entity.getCompletedAt());
  }
}
