package com.group34.eval_suite.runs.events;

import com.group34.eval_suite.runs.dto.RunResponse;

public record RunEventResponse(String type, RunResponse run) {

  public static RunEventResponse created(final RunResponse run) {
    return new RunEventResponse("RUN_CREATED", run);
  }
}
