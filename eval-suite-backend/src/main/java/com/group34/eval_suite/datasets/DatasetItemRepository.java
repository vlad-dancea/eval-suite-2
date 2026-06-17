package com.group34.eval_suite.datasets;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetItemRepository extends JpaRepository<DatasetItem, UUID> {}
