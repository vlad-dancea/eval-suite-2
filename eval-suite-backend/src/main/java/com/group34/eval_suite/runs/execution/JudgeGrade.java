package com.group34.eval_suite.runs.execution;

/**
 * Parsed result of a judge LLM call. {@code suggestion} is only populated for system-prompt grading
 * (it is empty for per-item output grading).
 */
public record JudgeGrade(int score, String feedback, String suggestion) {}
