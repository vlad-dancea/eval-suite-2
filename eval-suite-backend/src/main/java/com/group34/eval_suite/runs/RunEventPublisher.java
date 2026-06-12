package com.group34.eval_suite.runs;

import com.group34.eval_suite.runs.records.RunEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class RunEventPublisher {

  private static final long TIMEOUT_MS = 0L;
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public SseEmitter subscribe() {
    final SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onError(error -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    return emitter;
  }

  public void publish(RunEvent event) {
    for (final SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name(event.type()).data(event));
      } catch (IOException | IllegalStateException ex) {
        emitters.remove(emitter);
      }
    }
  }
}
