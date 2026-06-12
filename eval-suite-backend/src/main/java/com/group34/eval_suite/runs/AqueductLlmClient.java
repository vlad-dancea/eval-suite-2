package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.records.AqueductProperties;
import com.group34.eval_suite.runs.records.JudgeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@SuppressWarnings({"PMD.LongVariable", "PMD.UseObjectForClearerAPI"})
public class AqueductLlmClient {

  private static final Logger LOG = LoggerFactory.getLogger(AqueductLlmClient.class);

  private static final double DEFAULT_TEMPERATURE = 0.2;

  private final AqueductProperties properties;
  private final ObjectMapper objectMapper;
  private final ChatClient chatClient;

  public AqueductLlmClient(
      AqueductProperties properties,
      ObjectMapper objectMapper,
      ChatClient.Builder chatClientBuilder) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.chatClient = chatClientBuilder.build();
  }

  public String generateOutput(String systemPrompt, String input) {
    assertConfigured(properties.mainModel(), "main model");
    return complete(properties.mainModel(), DEFAULT_TEMPERATURE, systemPrompt, input);
  }

  public JudgeResult judgeOutput(
      String systemPrompt, String input, String modelOutput, String expectedOutput) {
    assertConfigured(properties.judgeModel(), "judge model");
    final String content =
        complete(
            properties.judgeModel(),
            0.0,
            """
            You are an evaluator for LLM outputs. Return only valid JSON with these fields:
            outputScore, promptScore, explanation, improvementSuggestion.
            Scores must be integers from 0 to 100.
            improvementSuggestion must contain ONLY the full text of an improved system prompt
            that would produce outputs closer to the expected output. Do not include any
            commentary, explanations, or surrounding text in improvementSuggestion — it is used
            verbatim as the next version of the system prompt.
            """,
            judgePrompt(systemPrompt, input, modelOutput, expectedOutput));
    return parseJudgeResult(content);
  }

  private String complete(
      String model, double temperature, String systemPrompt, String userPrompt) {
    LOG.debug("Calling Aqueduct model '{}' (temperature {})", model, temperature);
    final String content =
        chatClient
            .prompt()
            .options(OpenAiChatOptions.builder().model(model).temperature(temperature))
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content();
    if (!StringUtils.hasText(content)) {
      throw new IllegalStateException("Aqueduct returned an empty chat completion");
    }
    return content;
  }

  private JudgeResult parseJudgeResult(String content) {
    final String json = stripCodeFence(content);
    try {
      final JudgeResponse response = objectMapper.readValue(json, JudgeResponse.class);
      return new JudgeResult(
          clampScore(response.outputScore()),
          clampScore(response.promptScore()),
          response.explanation(),
          response.improvementSuggestion());
    } catch (JacksonException ex) {
      throw new IllegalStateException("Judge model returned invalid JSON: " + content, ex);
    }
  }

  private static String stripCodeFence(String content) {
    final String trimmed = content.trim();
    if (!trimmed.startsWith("```")) {
      return trimmed;
    }
    final int firstNewline = trimmed.indexOf('\n');
    final int lastFence = trimmed.lastIndexOf("```");
    if (firstNewline < 0 || lastFence <= firstNewline) {
      return trimmed;
    }
    return trimmed.substring(firstNewline + 1, lastFence).trim();
  }

  private static int clampScore(Integer score) {
    if (score == null) {
      return 0;
    }
    return Math.max(0, Math.min(100, score));
  }

  private static String judgePrompt(
      String systemPrompt, String input, String modelOutput, String expectedOutput) {
    return String.join(
        System.lineSeparator(),
        "Evaluate the model output against the expected output and input.",
        "",
        "Input:",
        input,
        "",
        "Model output:",
        modelOutput,
        "",
        "Expected output:",
        expectedOutput,
        "",
        "System prompt used for the run:",
        systemPrompt);
  }

  private static void assertConfigured(String value, String name) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalStateException("Aqueduct " + name + " is not configured");
    }
  }

  private record JudgeResponse(
      Integer outputScore, Integer promptScore, String explanation, String improvementSuggestion) {}
}
