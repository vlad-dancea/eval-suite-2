package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.dto.RunRequest;
import com.group34.eval_suite.runs.dto.RunResponse;
import com.group34.eval_suite.runs.entity.Run;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runs")
@CrossOrigin(origins = "*")
public class RunController {

  private final RunService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RunResponse createRun(@Valid @RequestBody final RunRequest request) {
    final Run run =
        service.createRun(
            request.systemPromptId(), request.datasetId(), request.automaticImprovementEnabled());
    return RunResponse.fromEntity(run);
  }

  @GetMapping
  public List<RunResponse> getRuns() {
    return service.getRuns().stream().map(RunResponse::fromEntity).toList();
  }
}
