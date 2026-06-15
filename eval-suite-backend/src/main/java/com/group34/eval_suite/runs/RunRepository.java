package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.entity.Run;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunRepository extends JpaRepository<Run, UUID> {

  List<Run> findByDeletedAtIsNullOrderByCreatedAtDesc();
}
