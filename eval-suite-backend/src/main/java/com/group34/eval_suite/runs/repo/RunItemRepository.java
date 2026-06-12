package com.group34.eval_suite.runs.repo;

import com.group34.eval_suite.runs.entity.RunItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunItemRepository extends JpaRepository<RunItem, UUID> {

  List<RunItem> findByRunIdOrderByPositionAsc(UUID runId);
}
