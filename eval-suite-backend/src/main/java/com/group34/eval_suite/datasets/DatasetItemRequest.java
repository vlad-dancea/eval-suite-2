package com.group34.eval_suite.datasets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatasetItemRequest(
    @NotBlank(message = "Input is required")
        @Size(max = 5000, message = "Input must not exceed 5000 characters")
        String input,
    @NotBlank(message = "Expected output is required")
        @Size(max = 5000, message = "Expected output must not exceed 5000 characters")
        String expectedOutput) {}
