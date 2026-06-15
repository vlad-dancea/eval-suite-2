package com.group34.eval_suite.systemprompts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class SystemPromptServiceTest {

  @Autowired private SystemPromptService service;

  @Autowired private SystemPromptRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
  }

  @Test
  void testCreateValidPromptStartsAtVersion1() {
    final SystemPrompt prompt = service.createPrompt("Test Prompt", "This is content", null);

    assertThat(prompt).isNotNull();
    assertThat(prompt.getId()).isNotNull();
    assertThat(prompt.getFamilyId()).isNotNull();
    assertThat(prompt.getVersionNumber()).isEqualTo(1);
    assertThat(prompt.getName()).isEqualTo("Test Prompt");
    assertThat(prompt.getContent()).isEqualTo("This is content");
    assertThat(prompt.getCreatedAt()).isNotNull();
    assertThat(prompt.getDeletedAt()).isNull();

    // Verify it is in DB
    final SystemPrompt dbPrompt = repository.findById(prompt.getId()).orElse(null);
    assertThat(dbPrompt).isNotNull();
  }

  @Test
  void testCreateNewVersionReusesFamilyIdAndIncrementsVersion() {
    final SystemPrompt v1 = service.createPrompt("Prompt V1", "Content 1", null);
    final UUID familyId = v1.getFamilyId();

    final SystemPrompt v2 = service.createPrompt("Prompt V2", "Content 2", familyId);

    assertThat(v2.getFamilyId()).isEqualTo(familyId);
    assertThat(v2.getVersionNumber()).isEqualTo(2);
    assertThat(v2.getName()).isEqualTo("Prompt V2");
    assertThat(v2.getContent()).isEqualTo("Content 2");

    final SystemPrompt v3 = service.createPrompt("Prompt V3", "Content 3", familyId);
    assertThat(v3.getFamilyId()).isEqualTo(familyId);
    assertThat(v3.getVersionNumber()).isEqualTo(3);
  }

  @Test
  void testCreateNewVersionForNonExistentFamilyThrowsNotFound() {
    final UUID randomFamilyId = UUID.randomUUID();
    assertThatThrownBy(() -> service.createPrompt("Name", "Content", randomFamilyId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Prompt family not found");
  }

  @Test
  void testContentConstraints() {
    // Empty content
    assertThatThrownBy(() -> service.createPrompt("Name", "", null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Content cannot be empty");

    // Max limit constraint (5000 characters)
    final String tooLongContent = "a".repeat(5001);
    assertThatThrownBy(() -> service.createPrompt("Name", tooLongContent, null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Content cannot exceed 5000 characters");
  }

  @Test
  void testDuplicateFamilyIdAndVersionIsRejectedByDb() {
    final SystemPrompt prompt = service.createPrompt("Prompt V1", "Content 1", null);

    final SystemPrompt duplicate =
        SystemPrompt.builder()
            .id(UUID.randomUUID())
            .familyId(prompt.getFamilyId())
            .versionNumber(1) // Duplicate version
            .name("Duplicate name")
            .content("Duplicate content")
            .createdAt(OffsetDateTime.now())
            .build();

    assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void testArchiveSetsDeletedAtForAllVersionsInFamily() {
    final SystemPrompt v1 = service.createPrompt("Prompt V1", "Content 1", null);
    final UUID familyId = v1.getFamilyId();
    final SystemPrompt v2 = service.createPrompt("Prompt V2", "Content 2", familyId);

    final SystemPrompt archived = service.archivePrompt(v1.getId());
    assertThat(archived.getDeletedAt()).isNotNull();

    // Verify all versions in family have deletedAt set
    final List<SystemPrompt> history = repository.findByFamilyIdOrderByVersionNumberDesc(familyId);
    assertThat(history).hasSize(2);
    assertThat(history.get(0).getDeletedAt()).isNotNull();
    assertThat(history.get(1).getDeletedAt()).isNotNull();
  }

  @Test
  void testActivePromptsQueriesExcludeArchivedPrompts() {
    final SystemPrompt p1 = service.createPrompt("Prompt 1", "Content 1", null);
    final SystemPrompt p2 = service.createPrompt("Prompt 2", "Content 2", null);

    final List<SystemPrompt> activeBefore = service.getActivePrompts();
    assertThat(activeBefore).hasSize(2);

    // Archive p1
    service.archivePrompt(p1.getId());

    final List<SystemPrompt> activeAfter = service.getActivePrompts();
    assertThat(activeAfter).hasSize(1);
    assertThat(activeAfter.get(0).getId()).isEqualTo(p2.getId());
  }

  @Test
  void testVersionHistoryQueriesReturnAllVersionsSorted() {
    final SystemPrompt v1 = service.createPrompt("Prompt V1", "Content 1", null);
    final UUID familyId = v1.getFamilyId();
    service.createPrompt("Prompt V2", "Content 2", familyId);
    service.createPrompt("Prompt V3", "Content 3", familyId);

    final List<SystemPrompt> history = service.getFamilyHistory(familyId);
    assertThat(history).hasSize(3);
    assertThat(history.get(0).getVersionNumber()).isEqualTo(3);
    assertThat(history.get(1).getVersionNumber()).isEqualTo(2);
    assertThat(history.get(2).getVersionNumber()).isEqualTo(1);
  }
}
