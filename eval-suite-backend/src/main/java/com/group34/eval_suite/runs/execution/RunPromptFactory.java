package com.group34.eval_suite.runs.execution;

import com.group34.eval_suite.ai.openai.dto.OpenAiChatMessage;
import java.util.List;
import org.springframework.stereotype.Component;

/** Builds the message sets for the four kinds of LLM calls a run makes. */
@Component
public class RunPromptFactory {

  private static final String ROLE_SYSTEM = "system";
  private static final String ROLE_USER = "user";

  private static final String OUTPUT_GRADING_INSTRUCTION =
      "You are a strict evaluation judge. Compare the assistant's actual output to the expected"
          + " output for the given input. Respond with ONLY a JSON object of the form"
          + " {\"score\": <integer 0-100>, \"feedback\": <string>}. score is how well the actual"
          + " output matches the expected output (100 = perfect, 0 = completely wrong). feedback is"
          + " a brief explanation of at most 2 sentences. Do not include any text outside the JSON.";

  private static final String PROMPT_GRADING_INSTRUCTION =
      "You are a strict evaluation judge assessing the quality of a system prompt based on how the"
          + " assistant performed across a dataset. Respond with ONLY a JSON object of the form"
          + " {\"score\": <integer 0-100>, \"feedback\": <string>, \"suggestion\": <string>}. score"
          + " grades the system prompt itself (100 = excellent, 0 = poor). feedback explains the"
          + " grade in at most 3 sentences. suggestion is concrete advice to improve the prompt in"
          + " at most 3 sentences. Do not include any text outside the JSON.";

  private static final String IMPROVEMENT_INSTRUCTION =
      "You improve system prompts. Given the current system prompt and how the assistant performed"
          + " on a dataset, rewrite the system prompt so the assistant's outputs better match the"
          + " expected outputs. Respond with ONLY the improved system prompt text and nothing else"
          + " — no explanations, no quotes, no Markdown.";

  /** Generation: run the system prompt against a single dataset input. */
  public List<OpenAiChatMessage> generation(final String systemPromptContent, final String input) {
    return List.of(
        new OpenAiChatMessage(ROLE_SYSTEM, systemPromptContent),
        new OpenAiChatMessage(ROLE_USER, input));
  }

  /** Output grading: grade one actual output against the expected output. */
  public List<OpenAiChatMessage> outputGrading(
      final String input, final String expectedOutput, final String actualOutput) {
    final String body =
        "Input:\n"
            + input
            + "\n\nExpected output:\n"
            + expectedOutput
            + "\n\nActual output:\n"
            + actualOutput;
    return List.of(
        new OpenAiChatMessage(ROLE_SYSTEM, OUTPUT_GRADING_INSTRUCTION),
        new OpenAiChatMessage(ROLE_USER, body));
  }

  /** Prompt grading: grade the system prompt given all results. */
  public List<OpenAiChatMessage> promptGrading(
      final String systemPromptContent, final List<EvaluatedItem> items) {
    final String body =
        "System prompt:\n" + systemPromptContent + "\n\nResults:\n" + renderResults(items);
    return List.of(
        new OpenAiChatMessage(ROLE_SYSTEM, PROMPT_GRADING_INSTRUCTION),
        new OpenAiChatMessage(ROLE_USER, body));
  }

  /** Improvement: ask for a better system prompt given the current one and all results. */
  public List<OpenAiChatMessage> improvement(
      final String systemPromptContent, final List<EvaluatedItem> items) {
    final String body =
        "Current system prompt:\n"
            + systemPromptContent
            + "\n\nResults:\n"
            + renderResults(items)
            + "\nRewrite the system prompt to improve these results.";
    return List.of(
        new OpenAiChatMessage(ROLE_SYSTEM, IMPROVEMENT_INSTRUCTION),
        new OpenAiChatMessage(ROLE_USER, body));
  }

  private String renderResults(final List<EvaluatedItem> items) {
    final StringBuilder builder = new StringBuilder();
    int index = 1;
    for (final EvaluatedItem item : items) {
      builder
          .append("Example ")
          .append(index)
          .append(":\nInput: ")
          .append(item.input())
          .append("\nExpected: ")
          .append(item.expectedOutput())
          .append("\nActual: ")
          .append(item.modelOutput())
          .append("\nScore: ")
          .append(item.outputScore())
          .append("\n\n");
      index++;
    }
    return builder.toString();
  }
}
