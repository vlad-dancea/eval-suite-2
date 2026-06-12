package com.group34.eval_suite.runs.repo;

import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.enums.RunStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("PMD.ShortVariable")
public interface RunRepository extends JpaRepository<Run, UUID> {

  List<Run> findByDeletedAtIsNullOrderByCreatedAtDesc();

  List<Run> findByStatusInAndDeletedAtIsNull(List<RunStatus> statuses);

  @Query(
      """
          SELECT DISTINCT r FROM Run r
          LEFT JOIN FETCH r.items
          WHERE r.id = :id AND r.deletedAt IS NULL
      """)
  Optional<Run> findActiveByIdWithItems(@Param("id") UUID id);

  @Query(
      """
          SELECT DISTINCT r FROM Run r
          LEFT JOIN FETCH r.items
          WHERE r.id = :id
      """)
  Optional<Run> findByIdWithItems(@Param("id") UUID id);
}
