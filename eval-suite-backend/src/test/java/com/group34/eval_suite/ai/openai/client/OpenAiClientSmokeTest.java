package com.group34.eval_suite.ai.openai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.group34.eval_suite.ai.openai.config.OpenAiProperties;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatChoice;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionRequest;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionResponse;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatMessage;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class OpenAiClientSmokeTest {

  private static final Logger LOGGER = Logger.getLogger(OpenAiClientSmokeTest.class.getName());
  private static final String DEFAULT_BASE_URL = "https://aqueduct.ai.datalab.tuwien.ac.at/";

  @Test
  void testAqueductChatCompletion() {
    final String apiKey = System.getenv("AQUEDUCT_API_KEY");
    final String modelName = System.getenv("AQUEDUCT_MODEL_NAME");
    final String baseUrl =
        System.getenv().getOrDefault("AQUEDUCT_OPENAI_BASE_URL", DEFAULT_BASE_URL);

    assumeTrue(apiKey != null && !apiKey.isBlank(), "AQUEDUCT_API_KEY is not set");
    assumeTrue(modelName != null && !modelName.isBlank(), "AQUEDUCT_MODEL_NAME is not set");

    final URI baseUri = URI.create(baseUrl);
    assumeTrue(
        canResolve(baseUri),
        () ->
            "Could not resolve Aqueduct host "
                + baseUri.getHost()
                + ". Check AQUEDUCT_OPENAI_BASE_URL or connect to the required network/VPN.");

    final OpenAiClient client =
        new AqueductOpenAiClient(new OpenAiProperties(baseUri, apiKey, modelName));

    final OpenAiChatCompletionResponse response =
        client.createChatCompletion(
            new OpenAiChatCompletionRequest(
                List.of(
                    new OpenAiChatMessage(
                        "system",
                        "Answer immediately. Do not explain. Output only the requested text."),
                    new OpenAiChatMessage("user", "/no_think\nReply with exactly: ok")),
                0.0,
                1024));

    assertThat(response.choices()).isNotEmpty();
    final OpenAiChatChoice firstChoice = response.choices().getFirst();
    assertThat(firstChoice.message().content())
        .as(
            "Expected non-empty assistant content. finishReason=%s, model=%s, usage=%s",
            firstChoice.finishReason(), response.model(), response.usage())
        .isNotBlank();

    LOGGER.info(() -> "Aqueduct smoke test response: " + firstChoice.message().content());
  }

  private boolean canResolve(final URI baseUri) {
    try {
      InetAddress.getByName(baseUri.getHost());
      return true;
    } catch (final UnknownHostException exception) {
      return false;
    }
  }
}
