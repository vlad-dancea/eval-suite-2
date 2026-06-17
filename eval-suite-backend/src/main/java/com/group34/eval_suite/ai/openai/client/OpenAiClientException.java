package com.group34.eval_suite.ai.openai.client;

public class OpenAiClientException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public OpenAiClientException(final String message) {
    super(message);
  }

  public OpenAiClientException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
