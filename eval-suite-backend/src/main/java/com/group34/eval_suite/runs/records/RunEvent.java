package com.group34.eval_suite.runs.records;

import com.group34.eval_suite.runs.dto.RunSummaryResponse;
import java.util.UUID;

public record RunEvent(String type, UUID runId, RunSummaryResponse run) {}
