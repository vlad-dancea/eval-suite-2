package com.group34.eval_suite.runs.entity;

import com.group34.eval_suite.runs.enums.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "run_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"PMD.LongVariable", "PMD.ShortVariable"})
public class RunItem {

  private static final String TEXT = "text";

  @Id
  @NotNull
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "run_id", nullable = false, updatable = false)
  private Run run;

  @NotNull
  @Column(name = "dataset_item_id", updatable = false, nullable = false)
  private UUID datasetItemId;

  @NotNull
  @Min(0)
  @Column(name = "position", updatable = false, nullable = false)
  private Integer position;

  @NotBlank
  @Size(min = 1, max = 5000)
  @Column(name = "input", updatable = false, nullable = false, length = 5000)
  private String input;

  @NotBlank
  @Size(min = 1, max = 5000)
  @Column(name = "expected_output", updatable = false, nullable = false, length = 5000)
  private String expectedOutput;

  @Column(name = "model_output", columnDefinition = TEXT)
  private String modelOutput;

  @Min(0)
  @Max(100)
  @Column(name = "output_score")
  private Integer outputScore;

  @Min(0)
  @Max(100)
  @Column(name = "prompt_score")
  private Integer promptScore;

  @Column(name = "judge_explanation", columnDefinition = TEXT)
  private String judgeExplanation;

  @Column(name = "improvement_suggestion", columnDefinition = TEXT)
  private String improvementSuggestion;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private RunStatus status;

  @Column(name = "error_message", columnDefinition = TEXT)
  private String errorMessage;

  @NotNull
  @Column(name = "created_at", updatable = false, nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;
}
