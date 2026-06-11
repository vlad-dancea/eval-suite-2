package com.group34.eval_suite.dto;

import com.group34.eval_suite.model.SystemPrompt;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings("PMD.ShortVariable")
public record SystemPromptResponse(
    UUID id,
    UUID familyId,
    int versionNumber,
    String name,
    String content,
    String preview,
    OffsetDateTime createdAt,
    OffsetDateTime deletedAt) {
  public static SystemPromptResponse fromEntity(SystemPrompt entity) {
    final String content = entity.getContent();
    final String preview = content.length() <= 100 ? content : content.substring(0, 100);
    return new SystemPromptResponse(
        entity.getId(),
        entity.getFamilyId(),
        entity.getVersionNumber(),
        entity.getName(),
        content,
        preview,
        entity.getCreatedAt(),
        entity.getDeletedAt());
  }
}
