package com.group34.eval_suite.ai.openai.client;

import com.group34.eval_suite.ai.openai.config.OpenAiProperties;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatChoice;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionRequest;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionResponse;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatMessage;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatUsage;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

@SuppressWarnings("PMD.LawOfDemeter")
public class AqueductOpenAiClient implements OpenAiClient {

  private final OpenAiProperties properties;
  private final OpenAiChatModel chatModel;

  public AqueductOpenAiClient(final OpenAiProperties properties) {
    this.properties = properties;
    this.chatModel =
        OpenAiChatModel.builder()
            .options(
                OpenAiChatOptions.builder()
                    .baseUrl(properties.baseUrl().toString())
                    .apiKey(properties.apiKey())
                    .build())
            .build();
  }

  @Override
  public OpenAiChatCompletionResponse createChatCompletion(
      final OpenAiChatCompletionRequest request) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new OpenAiClientException("Aqueduct API key is not configured");
    }
    if (properties.modelName() == null || properties.modelName().isBlank()) {
      throw new OpenAiClientException("Aqueduct model name is not configured");
    }

    return toResponse(
        chatModel.call(new Prompt(toMessages(request.messages()), toOptions(request))));
  }

  private OpenAiChatOptions toOptions(final OpenAiChatCompletionRequest request) {
    return OpenAiChatOptions.builder()
        .model(properties.modelName())
        .temperature(request.temperature())
        .maxCompletionTokens(request.maxCompletionTokens())
        .build();
  }

  private List<Message> toMessages(final List<OpenAiChatMessage> messages) {
    return messages.stream().map(this::toMessage).toList();
  }

  private Message toMessage(final OpenAiChatMessage message) {
    return switch (message.role()) {
      case "system" -> new SystemMessage(message.content());
      case "assistant" -> new AssistantMessage(message.content());
      case "user" -> new UserMessage(message.content());
      default ->
          throw new OpenAiClientException("Unsupported OpenAI message role: " + message.role());
    };
  }

  private OpenAiChatCompletionResponse toResponse(final ChatResponse response) {
    final ChatResponseMetadata metadata = response.getMetadata();

    return new OpenAiChatCompletionResponse(
        metadata.getId(),
        metadata.getModel(),
        toChoices(response.getResults()),
        toUsage(metadata.getUsage()),
        null);
  }

  private List<OpenAiChatChoice> toChoices(final List<Generation> generations) {
    final List<OpenAiChatChoice> choices = new ArrayList<>(generations.size());
    for (int index = 0; index < generations.size(); index++) {
      final Generation generation = generations.get(index);
      choices.add(
          new OpenAiChatChoice(
              index,
              new OpenAiChatMessage("assistant", generation.getOutput().getText()),
              generation.getMetadata().getFinishReason()));
    }
    return List.copyOf(choices);
  }

  private OpenAiChatUsage toUsage(final Usage usage) {
    if (usage == null) {
      return null;
    }
    return new OpenAiChatUsage(
        usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
  }
}
