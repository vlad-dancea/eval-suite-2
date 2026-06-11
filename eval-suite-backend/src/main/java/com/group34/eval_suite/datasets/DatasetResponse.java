package com.group34.eval_suite.dto;

import com.group34.eval_suite.model.Dataset;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.ShortVariable")
public record DatasetResponse(
    UUID id,
    String name,
    int itemCount,
    OffsetDateTime createdAt,
    OffsetDateTime deletedAt,
    List<DatasetItemResponse> items) {

  public DatasetResponse {
    items = items == null ? List.of() : List.copyOf(items);
  }

  @Override
  public List<DatasetItemResponse> items() {
    return List.copyOf(items);
  }

  public static DatasetResponse fromEntity(Dataset entity) {
    final List<DatasetItemResponse> items =
        entity.getItems().stream().map(DatasetItemResponse::fromEntity).toList();
    return new DatasetResponse(
        entity.getId(),
        entity.getName(),
        items.size(),
        entity.getCreatedAt(),
        entity.getDeletedAt(),
        items);
  }
}
