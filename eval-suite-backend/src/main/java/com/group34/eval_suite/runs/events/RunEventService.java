package com.group34.eval_suite.runs.events;

import com.group34.eval_suite.runs.dto.RunResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class RunEventService {

  private static final long TIMEOUT_MILLIS = 30L * 60L * 1000L;
  private static final String RUN_CREATED = "RUN_CREATED";
  private static final String RUN_UPDATED = "RUN_UPDATED";
  private static final String RUN_FAILED = "RUN_FAILED";

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public SseEmitter connect() {
    final SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError(unused -> emitters.remove(emitter));

    try {
      emitter.send(SseEmitter.event().comment("connected"));
    } catch (final IOException exception) {
      emitters.remove(emitter);
      emitter.completeWithError(exception);
    }

    return emitter;
  }

  public void publishRunCreated(final RunResponse run) {
    publish(RUN_CREATED, RunEventResponse.created(run));
  }

  public void publishRunUpdated(final RunResponse run) {
    publish(RUN_UPDATED, RunEventResponse.updated(run));
  }

  public void publishRunFailed(final RunResponse run) {
    publish(RUN_FAILED, RunEventResponse.failed(run));
  }

  private void publish(final String eventName, final RunEventResponse event) {
    for (final SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name(eventName).data(event));
      } catch (final IOException exception) {
        emitters.remove(emitter);
        emitter.completeWithError(exception);
      }
    }
  }
}
