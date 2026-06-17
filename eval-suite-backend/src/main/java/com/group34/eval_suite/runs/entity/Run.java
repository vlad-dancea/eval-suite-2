package com.group34.eval_suite.runs.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({
  "PMD.ShortVariable",
  "PMD.ShortClassName",
  "PMD.TooManyFields",
  "PMD.LongVariable"
})
public class Run {

  private static final String TEXT_COLUMN = "text";

  @Id
  @NotNull
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "created_at", updatable = false, nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @NotNull
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private RunStatus status = RunStatus.QUEUED;

  @NotNull
  @Column(name = "system_prompt_id", updatable = false, nullable = false)
  private UUID systemPromptId;

  @NotNull
  @Column(name = "dataset_id", updatable = false, nullable = false)
  private UUID datasetId;

  @Min(0)
  @Max(100)
  @Column(name = "output_score")
  private Integer outputScore;

  @Min(0)
  @Max(100)
  @Column(name = "system_prompt_score")
  private Integer systemPromptScore;

  @Column(name = "system_prompt_feedback", columnDefinition = TEXT_COLUMN)
  private String systemPromptFeedback;

  @Column(name = "system_prompt_improvement_suggestion", columnDefinition = TEXT_COLUMN)
  private String systemPromptImprovementSuggestion;

  @Column(name = "automatic_improvement_enabled", nullable = false)
  private boolean automaticImprovementEnabled;

  @Min(0)
  @Column(name = "automatic_improvement_attempt")
  private Integer automaticImprovementAttempt;

  @Column(name = "failure_message", columnDefinition = TEXT_COLUMN)
  private String failureMessage;

  @Valid
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "run_item_results", joinColumns = @JoinColumn(name = "run_id"))
  @OrderBy("position ASC")
  @Builder.Default
  private List<RunDatasetItemResult> itemResults = new ArrayList<>();
}
