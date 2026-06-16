package com.group34.eval_suite.ai.openai.dto;

import jakarta.validation.constraints.NotBlank;

public record OpenAiChatMessage(@NotBlank String role, @NotBlank String content) {}
