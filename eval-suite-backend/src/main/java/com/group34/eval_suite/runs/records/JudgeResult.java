package com.group34.eval_suite.runs.records;

@SuppressWarnings("PMD.LongVariable")
public record JudgeResult(
    int outputScore, int promptScore, String explanation, String improvementSuggestion) {}
