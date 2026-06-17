package com.group34.eval_suite.runs.execution;

import java.util.List;

/** Outcome of evaluating a single run: the judge's grade of the system prompt (drives the
 * automatic-improvement loop), the prompt content that was evaluated, and the per-item results. */
public record EvaluationResult(
    int systemPromptScore, String promptContent, List<EvaluatedItem> items) {}
