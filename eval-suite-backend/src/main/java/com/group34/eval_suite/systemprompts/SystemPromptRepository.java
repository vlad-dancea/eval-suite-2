package com.group34.eval_suite.systemprompts;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemPromptRepository extends JpaRepository<SystemPrompt, UUID> {

  /**
   * Archive all active prompts in a family.
   */
  @Modifying
  @Query(
      "UPDATE SystemPrompt sp SET sp.deletedAt = :deletedAt WHERE sp.familyId = :familyId AND"
          + " sp.deletedAt IS NULL")
  void archiveFamily(
      @Param("familyId") UUID familyId, @Param("deletedAt") OffsetDateTime deletedAt);

  /**
   * Find all active latest versions of prompts.
   * A prompt is considered active if deleted_at is null.
   * We retrieve the latest version for each family.
   */
  @Query(
      """
          SELECT sp FROM SystemPrompt sp
          WHERE sp.deletedAt IS NULL
          AND sp.versionNumber = (
              SELECT MAX(sub.versionNumber)
              FROM SystemPrompt sub
              WHERE sub.familyId = sp.familyId
              AND sub.deletedAt IS NULL
          )
          ORDER BY sp.createdAt DESC
      """)
  List<SystemPrompt> findAllActiveLatest();

  /**
   * Find the max version number for a given family id.
   */
  @Query("SELECT MAX(sp.versionNumber) FROM SystemPrompt sp WHERE sp.familyId = :familyId")
  Integer findMaxVersionByFamilyId(@Param("familyId") UUID familyId);

  /**
   * Find all versions for a given family id sorted by version number descending.
   */
  List<SystemPrompt> findByFamilyIdOrderByVersionNumberDesc(UUID familyId);
}
