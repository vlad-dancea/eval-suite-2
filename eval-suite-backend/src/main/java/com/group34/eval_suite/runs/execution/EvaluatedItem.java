package com.group34.eval_suite.runs.execution;

/** One dataset item evaluated during a run: its input, expected output, and the model's actual
 * output plus the judge's score. Used to build the system-prompt grading and improvement prompts. */
public record EvaluatedItem(
    String input, String expectedOutput, String modelOutput, Integer outputScore) {}
