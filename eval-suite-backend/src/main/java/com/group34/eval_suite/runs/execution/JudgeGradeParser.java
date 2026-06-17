package com.group34.eval_suite.runs.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Parses the JSON object a judge model is instructed to return. Tolerates surrounding prose or
 * Markdown code fences by extracting the outermost {@code { ... }} block, and clamps the score to
 * the valid 0-100 range.
 */
@Component
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class JudgeGradeParser {

  private static final int MIN_SCORE = 0;
  private static final int MAX_SCORE = 100;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public JudgeGrade parse(final String content) {
    final String json = extractJsonObject(content);
    final JsonNode node;
    try {
      node = objectMapper.readTree(json);
    } catch (final com.fasterxml.jackson.core.JsonProcessingException exception) {
      throw new RunExecutionException("Judge response was not valid JSON: " + content, exception);
    }

    final JsonNode scoreNode = node.get("score");
    if (scoreNode == null || scoreNode.isNull()) {
      throw new RunExecutionException("Judge response is missing a score: " + content);
    }

    final int score = clamp(readScore(scoreNode, content));
    final String feedback = node.path("feedback").asText("");
    final String suggestion = node.path("suggestion").asText("");
    return new JudgeGrade(score, feedback, suggestion);
  }

  private int readScore(final JsonNode scoreNode, final String content) {
    if (scoreNode.isNumber()) {
      return scoreNode.asInt();
    }
    try {
      return Integer.parseInt(scoreNode.asText().trim());
    } catch (final NumberFormatException exception) {
      throw new RunExecutionException("Judge score was not a number: " + content, exception);
    }
  }

  private int clamp(final int score) {
    return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
  }

  private String extractJsonObject(final String content) {
    if (content == null) {
      throw new RunExecutionException("Judge response was empty");
    }
    final int start = content.indexOf('{');
    final int end = content.lastIndexOf('}');
    if (start < 0 || end <= start) {
      throw new RunExecutionException("Judge response did not contain a JSON object: " + content);
    }
    return content.substring(start, end + 1);
  }
}
