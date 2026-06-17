package com.group34.eval_suite.systemprompts;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.ShortVariable", "PMD.CyclomaticComplexity", "PMD.LongVariable"})
public class SystemPromptService {

  private static final int MAX_CONTENT_LENGTH = 5000;
  private static final int MAX_NAME_LENGTH = 120;

  private final SystemPromptRepository repository;

  /**
   * Create a new system prompt.
   * If familyId is null, it's the first version (v1) of a new prompt.
   * If familyId is provided, it increments the version number.
   */
  @Transactional
  public SystemPrompt createPrompt(final String name, final String content, final UUID familyId) {
    if (content == null || content.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content cannot be empty");
    }
    if (content.length() > MAX_CONTENT_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
    }
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Name cannot exceed " + MAX_NAME_LENGTH + " characters");
    }

    final UUID finalFamilyId;
    final int versionNumber;

    if (familyId == null) {
      finalFamilyId = UUID.randomUUID();
      versionNumber = 1;
    } else {
      // Verify the family exists first
      final List<SystemPrompt> versions =
          repository.findByFamilyIdOrderByVersionNumberDesc(familyId);
      if (versions.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt family not found");
      }
      finalFamilyId = familyId;
      final Integer maxVersion = repository.findMaxVersionByFamilyId(familyId);
      versionNumber = (maxVersion != null ? maxVersion : 0) + 1;
    }

    final SystemPrompt prompt =
        SystemPrompt.builder()
            .id(UUID.randomUUID())
            .familyId(finalFamilyId)
            .versionNumber(versionNumber)
            .name(name)
            .content(content)
            .createdAt(OffsetDateTime.now())
            .deletedAt(null)
            .build();

    return repository.save(prompt);
  }

  /**
   * Find a specific system prompt by ID.
   */
  public SystemPrompt getPromptById(final UUID id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "System prompt not found"));
  }

  /**
   * Get all active, latest versions of prompts.
   */
  public List<SystemPrompt> getActivePrompts() {
    return repository.findAllActiveLatest();
  }

  /**
   * Get all versions of a prompt family.
   */
  public List<SystemPrompt> getFamilyHistory(final UUID familyId) {
    final List<SystemPrompt> history = repository.findByFamilyIdOrderByVersionNumberDesc(familyId);
    if (history.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt family not found");
    }
    return history;
  }

  /**
   * Discard a single prompt version by setting its deleted_at timestamp.
   * Unlike {@link #archivePrompt(UUID)} this only affects the one version, so the previous
   * version becomes the active latest again. Used to drop an auto-improvement attempt that did
   * not improve on the previous version.
   */
  @Transactional
  public SystemPrompt discardVersion(final UUID id) {
    final SystemPrompt prompt = getPromptById(id);
    if (prompt.getDeletedAt() == null) {
      prompt.setDeletedAt(OffsetDateTime.now());
      repository.save(prompt);
    }
    return prompt;
  }

  /**
   * Archive a system prompt by setting its deleted_at timestamp.
   * This archives all prompts in the same family.
   */
  @Transactional
  public SystemPrompt archivePrompt(final UUID id) {
    final SystemPrompt prompt = getPromptById(id);
    final OffsetDateTime now = OffsetDateTime.now();
    repository.archiveFamily(prompt.getFamilyId(), now);
    prompt.setDeletedAt(now);
    return prompt;
  }
}
