package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.dto.RunDetailResponse;
import com.group34.eval_suite.runs.dto.RunRequest;
import com.group34.eval_suite.runs.dto.RunResponse;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.events.RunEventService;
import com.group34.eval_suite.runs.execution.RunExecutionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
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
public class RunController {

  private final RunService service;
  private final RunEventService eventService;
  private final RunExecutionService executionService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RunResponse createRun(@Valid @RequestBody final RunRequest request) {
    final Run run =
        service.createRun(
            request.systemPromptId(), request.datasetId(), request.automaticImprovementEnabled());
    final RunResponse response = service.toResponse(run);
    eventService.publishRunCreated(response);
    executionService.executeAsync(run.getId());
    return response;
  }

  @GetMapping
  public List<RunResponse> getRuns() {
    return service.getRuns();
  }

  @GetMapping("/{id}")
  public RunDetailResponse getRun(@PathVariable("id") final UUID id) {
    return service.getRunDetail(id);
  }

  @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter getRunEvents() {
    return eventService.connect();
  }
}
