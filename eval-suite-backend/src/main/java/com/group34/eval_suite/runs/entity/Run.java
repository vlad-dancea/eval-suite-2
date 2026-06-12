package com.group34.eval_suite.runs.entity;

import com.group34.eval_suite.runs.enums.RunStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
  "PMD.LongVariable",
  "PMD.ShortClassName",
  "PMD.ShortVariable",
  "PMD.TooManyFields"
})
public class Run {

  @Id
  @NotNull
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "system_prompt_id", updatable = false, nullable = false)
  private UUID systemPromptId;

  @NotNull
  @Column(name = "system_prompt_family_id", updatable = false, nullable = false)
  private UUID systemPromptFamilyId;

  @NotNull
  @Min(1)
  @Column(name = "system_prompt_version_number", updatable = false, nullable = false)
  private Integer systemPromptVersionNumber;

  @NotBlank
  @Size(max = 120)
  @Column(name = "system_prompt_name", updatable = false, nullable = false, length = 120)
  private String systemPromptName;

  @NotBlank
  @Size(min = 1, max = 5000)
  @Column(name = "system_prompt_content", updatable = false, nullable = false, length = 5000)
  private String systemPromptContent;

  @NotNull
  @Column(name = "dataset_id", updatable = false, nullable = false)
  private UUID datasetId;

  @NotBlank
  @Size(max = 120)
  @Column(name = "dataset_name", updatable = false, nullable = false, length = 120)
  private String datasetName;

  @NotNull
  @Min(1)
  @Column(name = "dataset_item_count", updatable = false, nullable = false)
  private Integer datasetItemCount;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private RunStatus status;

  @Min(0)
  @Max(100)
  @Column(name = "average_output_score", precision = 5, scale = 2)
  private BigDecimal averageOutputScore;

  @Min(0)
  @Max(100)
  @Column(name = "average_prompt_score", precision = 5, scale = 2)
  private BigDecimal averagePromptScore;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @NotNull
  @Column(name = "created_at", updatable = false, nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @Valid
  @NotEmpty
  @OneToMany(
      mappedBy = "run",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("position ASC")
  @Builder.Default
  private List<RunItem> items = new ArrayList<>();
}
