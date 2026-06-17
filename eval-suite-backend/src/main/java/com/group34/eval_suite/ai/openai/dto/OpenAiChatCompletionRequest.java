package com.group34.eval_suite.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

@SuppressWarnings("PMD.LongVariable")
public record OpenAiChatCompletionRequest(
    @NotEmpty List<@Valid OpenAiChatMessage> messages,
    Double temperature,
    @Positive @JsonProperty("max_completion_tokens") Integer maxCompletionTokens) {

  public OpenAiChatCompletionRequest {
    messages = List.copyOf(messages);
  }
}
