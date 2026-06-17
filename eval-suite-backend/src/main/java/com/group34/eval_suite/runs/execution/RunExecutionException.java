package com.group34.eval_suite.runs.execution;

/** Raised when a run pipeline cannot complete (LLM error, unparseable judge output, missing data). */
public class RunExecutionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RunExecutionException(final String message) {
    super(message);
  }

  public RunExecutionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
