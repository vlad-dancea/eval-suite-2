package com.group34.eval_suite.runs.dto;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.ShortVariable", "PMD.LongVariable"})
public record RunDetailResponse(RunResponse run, List<RunItemResultResponse> items) {

  public record RunItemResultResponse(
      UUID datasetItemId,
      Integer position,
      String input,
      String expectedOutput,
      String modelOutput,
      Integer outputScore,
      String judgeFeedback) {}
}
