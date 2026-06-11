package com.group34.eval_suite.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dataset_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.ShortVariable")
public class DatasetItem {

  @Id
  @NotNull
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

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
}
