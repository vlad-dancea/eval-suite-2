package com.group34.eval_suite.runs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunDatasetItemResult {

  private static final String TEXT_COLUMN = "text";

  @NotNull
  @Column(name = "dataset_item_id", updatable = false, nullable = false)
  private UUID datasetItemId;

  @NotNull
  @Min(0)
  @Column(name = "position", updatable = false, nullable = false)
  private Integer position;

  @Column(name = "model_output", columnDefinition = TEXT_COLUMN)
  private String modelOutput;

  @Min(0)
  @Max(100)
  @Column(name = "output_score")
  private Integer outputScore;

  @Column(name = "judge_feedback", columnDefinition = TEXT_COLUMN)
  private String judgeFeedback;
}
