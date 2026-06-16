package com.group34.eval_suite.ai.openai.client;

import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionRequest;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionResponse;

@FunctionalInterface
public interface OpenAiClient {

  OpenAiChatCompletionResponse createChatCompletion(OpenAiChatCompletionRequest request);
}
