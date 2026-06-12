package com.group34.eval_suite.runs.endpoint;

import com.group34.eval_suite.runs.RunEventPublisher;
import com.group34.eval_suite.runs.RunService;
import com.group34.eval_suite.runs.dto.RunRequest;
import com.group34.eval_suite.runs.dto.RunResponse;
import com.group34.eval_suite.runs.dto.RunSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runs")
@CrossOrigin(origins = "*")
@SuppressWarnings("PMD.ShortVariable")
public class RunController {

  private final RunService runService;
  private final RunEventPublisher runEventPublisher;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RunResponse createRun(@Valid @RequestBody RunRequest request) {
    return runService.createRun(request.systemPromptId(), request.datasetId());
  }

  @GetMapping
  public List<RunSummaryResponse> getRuns() {
    return runService.getActiveRuns();
  }

  @GetMapping("/{id}")
  public RunResponse getRun(@PathVariable("id") UUID id) {
    return runService.getRun(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRun(@PathVariable("id") UUID id) {
    runService.deleteRun(id);
  }

  @GetMapping("/events")
  public SseEmitter streamRunEvents() {
    return runEventPublisher.subscribe();
  }
}
