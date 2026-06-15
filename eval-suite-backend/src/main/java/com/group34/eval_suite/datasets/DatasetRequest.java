package com.group34.eval_suite.datasets;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DatasetRequest(
    @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must not exceed 120 characters")
        String name,
    @NotEmpty(message = "A dataset must contain at least one item") @Valid
        List<DatasetItemRequest> items) {

  public DatasetRequest {
    items = items == null ? List.of() : List.copyOf(items);
  }

  @Override
  public List<DatasetItemRequest> items() {
    return List.copyOf(items);
  }
}
