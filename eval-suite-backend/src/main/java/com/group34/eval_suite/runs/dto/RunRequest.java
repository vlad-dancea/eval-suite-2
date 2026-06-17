package com.group34.eval_suite.runs.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@SuppressWarnings("PMD.LongVariable")
public record RunRequest(
    @NotNull(message = "System prompt ID is required") UUID systemPromptId,
    @NotNull(message = "Dataset ID is required") UUID datasetId,
    boolean automaticImprovementEnabled) {}
