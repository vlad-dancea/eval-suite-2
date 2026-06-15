package com.group34.eval_suite.datasets;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("PMD.ShortVariable")
public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

  /**
   * List all active datasets as lightweight summaries with their item counts.
   * A dataset is active when deleted_at is null.
   */
  @Query(
      """
          SELECT d.id AS id, d.name AS name, SIZE(d.items) AS itemCount, d.createdAt AS createdAt
          FROM Dataset d
          WHERE d.deletedAt IS NULL
          ORDER BY d.createdAt DESC
      """)
  List<DatasetSummaryResponse> findActiveSummaries();

  /**
   * Fetch a single active dataset together with all of its items.
   */
  @Query(
      """
          SELECT DISTINCT d FROM Dataset d
          LEFT JOIN FETCH d.items
          WHERE d.id = :id AND d.deletedAt IS NULL
      """)
  Optional<Dataset> findActiveByIdWithItems(@Param("id") UUID id);
}
