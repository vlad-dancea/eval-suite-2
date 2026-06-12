package com.group34.eval_suite.runs.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RunRequest(@NotNull UUID systemPromptId, @NotNull UUID datasetId) {}
