package com.group34.eval_suite.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("PMD.ShortVariable")
public record OpenAiChatCompletionResponse(
    String id,
    String model,
    List<OpenAiChatChoice> choices,
    OpenAiChatUsage usage,
    @JsonProperty("created") Long createdAt) {

  public OpenAiChatCompletionResponse {
    choices = choices == null ? List.of() : List.copyOf(choices);
  }
}
