package com.group34.eval_suite.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lightweight projection of a dataset for list views, exposing the item count
 * without loading every item. Backed by a Spring Data interface projection.
 */
@SuppressWarnings("PMD.ShortVariable")
public interface DatasetSummaryResponse {
  UUID getId();

  String getName();

  long getItemCount();

  OffsetDateTime getCreatedAt();
}
