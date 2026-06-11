package com.group34.eval_suite.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "system_prompts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.ShortVariable")
public class SystemPrompt {

  @Id
  @NotNull
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "family_id", updatable = false, nullable = false)
  private UUID familyId;

  @NotNull
  @Min(1)
  @Column(name = "version_number", updatable = false, nullable = false)
  private Integer versionNumber;

  @NotBlank
  @Size(max = 120)
  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @NotBlank
  @Size(min = 1, max = 5000)
  @Column(name = "content", updatable = false, nullable = false, length = 5000)
  private String content;

  @NotNull
  @Column(name = "created_at", updatable = false, nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;
}
