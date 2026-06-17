package com.group34.eval_suite.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiChatChoice(
    Integer index, OpenAiChatMessage message, @JsonProperty("finish_reason") String finishReason) {}
