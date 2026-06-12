package com.group34.eval_suite.datasets;

import java.util.UUID;

@SuppressWarnings({"PMD.ShortVariable", "PMD.LongVariable"})
public record DatasetItemResponse(
    UUID id,
    int position,
    String input,
    String expectedOutput,
    String inputPreview,
    String expectedOutputPreview) {
  public static DatasetItemResponse fromEntity(DatasetItem entity) {
    final String input = entity.getInput();
    final String expectedOutput = entity.getExpectedOutput();
    return new DatasetItemResponse(
        entity.getId(),
        entity.getPosition(),
        input,
        expectedOutput,
        preview(input),
        preview(expectedOutput));
  }

  private static String preview(final String content) {
    return content.length() <= 100 ? content : content.substring(0, 100);
  }
}
