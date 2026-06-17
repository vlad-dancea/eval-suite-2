package com.group34.eval_suite.runs.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group34.eval_suite.ai.openai.client.OpenAiClient;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatChoice;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionRequest;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatCompletionResponse;
import com.group34.eval_suite.ai.openai.dto.OpenAiChatMessage;
import com.group34.eval_suite.datasets.Dataset;
import com.group34.eval_suite.datasets.DatasetItem;
import com.group34.eval_suite.datasets.DatasetRepository;
import com.group34.eval_suite.runs.RunRepository;
import com.group34.eval_suite.runs.RunService;
import com.group34.eval_suite.runs.dto.RunResponse;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunDatasetItemResult;
import com.group34.eval_suite.runs.entity.RunStatus;
import com.group34.eval_suite.runs.events.RunEventService;
import com.group34.eval_suite.systemprompts.SystemPrompt;
import com.group34.eval_suite.systemprompts.SystemPromptRepository;
import com.group34.eval_suite.systemprompts.SystemPromptService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RunExecutionServiceTest {

  private RunService runService;
  private SystemPromptService systemPromptService;
  private RunRepository runRepository;
  private SystemPromptRepository systemPromptRepository;
  private DatasetRepository datasetRepository;
  private RunEventService eventService;
  private FakeOpenAiClient openAiClient;
  private RunExecutionService service;

  @BeforeEach
  void setUp() {
    runService = mock(RunService.class);
    systemPromptService = mock(SystemPromptService.class);
    runRepository = mock(RunRepository.class);
    systemPromptRepository = mock(SystemPromptRepository.class);
    datasetRepository = mock(DatasetRepository.class);
    eventService = mock(RunEventService.class);
    openAiClient = new FakeOpenAiClient();

    service =
        new RunExecutionService(
            openAiClient,
            runService,
            systemPromptService,
            runRepository,
            systemPromptRepository,
            datasetRepository,
            eventService,
            new RunPromptFactory(),
            new JudgeGradeParser());

    when(runService.toResponse(any())).thenReturn(mock(RunResponse.class));
    when(runService.getRunResponse(any())).thenReturn(mock(RunResponse.class));
  }

  @Test
  void evaluatesRunSavingItemOutputsAggregateAndPromptGrade() {
    final UUID promptId = UUID.randomUUID();
    final UUID datasetId = UUID.randomUUID();
    final Run run = run(UUID.randomUUID(), promptId, datasetId, false, 0);

    when(runRepository.findById(run.getId())).thenReturn(Optional.of(run));
    when(runService.markRunning(any())).thenReturn(run);
    when(runService.completeRun(any(), any(), any(), any(), any(), any())).thenReturn(run);
    when(systemPromptRepository.findById(promptId)).thenReturn(Optional.of(prompt(promptId, "P1")));
    when(datasetRepository.findActiveByIdWithItems(datasetId))
        .thenReturn(Optional.of(datasetWithItems(datasetId, 2)));

    openAiClient.outputGrade = "{\"score\": 80, \"feedback\": \"ok\"}";
    openAiClient.promptGrades.add("{\"score\": 70, \"feedback\": \"f\", \"suggestion\": \"s\"}");

    service.executeAsync(run.getId());

    final ArgumentCaptor<Integer> outputScore = ArgumentCaptor.forClass(Integer.class);
    @SuppressWarnings("unchecked")
    final ArgumentCaptor<List<RunDatasetItemResult>> items =
        ArgumentCaptor.forClass(List.class);
    final ArgumentCaptor<Integer> promptScore = ArgumentCaptor.forClass(Integer.class);

    verify(runService)
        .completeRun(
            eq(run.getId()),
            outputScore.capture(),
            items.capture(),
            promptScore.capture(),
            eq("f"),
            eq("s"));
    assertThat(outputScore.getValue()).isEqualTo(80);
    assertThat(promptScore.getValue()).isEqualTo(70);
    assertThat(items.getValue()).hasSize(2);
    assertThat(items.getValue().getFirst().getModelOutput()).isEqualTo("OUT");
    verify(runService, never()).failRun(any(), anyString());
  }

  @Test
  void improvementLoopKeepsImprovingVersionsAndDiscardsTheFirstNonImprovement() {
    final UUID rootPromptId = UUID.randomUUID();
    final UUID familyId = UUID.randomUUID();
    final UUID datasetId = UUID.randomUUID();
    final Run rootRun = run(UUID.randomUUID(), rootPromptId, datasetId, true, 0);

    final Map<UUID, Run> runStore = new HashMap<>();
    runStore.put(rootRun.getId(), rootRun);
    final Map<UUID, SystemPrompt> promptStore = new HashMap<>();
    promptStore.put(rootPromptId, prompt(rootPromptId, familyId, 1, "P1"));
    final List<UUID> createdVersionIds = new ArrayList<>();

    when(runRepository.findById(any()))
        .thenAnswer(invocation -> Optional.ofNullable(runStore.get(invocation.getArgument(0))));
    when(systemPromptRepository.findById(any()))
        .thenAnswer(invocation -> Optional.ofNullable(promptStore.get(invocation.getArgument(0))));
    when(datasetRepository.findActiveByIdWithItems(datasetId))
        .thenReturn(Optional.of(datasetWithItems(datasetId, 1)));
    when(runService.markRunning(any())).thenReturn(rootRun);
    when(runService.completeRun(any(), any(), any(), any(), any(), any())).thenReturn(rootRun);

    when(systemPromptService.createPrompt(anyString(), anyString(), eq(familyId)))
        .thenAnswer(
            invocation -> {
              final int version = promptStore.size() + 1;
              final SystemPrompt created =
                  prompt(UUID.randomUUID(), familyId, version, invocation.getArgument(1));
              promptStore.put(created.getId(), created);
              createdVersionIds.add(created.getId());
              return created;
            });
    when(runService.createChildRun(any(), eq(datasetId), anyInt()))
        .thenAnswer(
            invocation -> {
              final Run child =
                  run(UUID.randomUUID(), invocation.getArgument(0), datasetId, false, 1);
              runStore.put(child.getId(), child);
              return child;
            });

    openAiClient.outputGrade = "{\"score\": 90, \"feedback\": \"ok\"}";
    // root = 50, attempt 1 improves to 70 (kept), attempt 2 drops to 60 (discarded, loop stops).
    openAiClient.promptGrades.add("{\"score\": 50, \"feedback\": \"f\", \"suggestion\": \"s\"}");
    openAiClient.promptGrades.add("{\"score\": 70, \"feedback\": \"f\", \"suggestion\": \"s\"}");
    openAiClient.promptGrades.add("{\"score\": 60, \"feedback\": \"f\", \"suggestion\": \"s\"}");
    openAiClient.improvedPrompts.add("P2");
    openAiClient.improvedPrompts.add("P3");

    service.executeAsync(rootRun.getId());

    assertThat(createdVersionIds).hasSize(2);
    // The second (non-improving) version is discarded; the first improvement is kept.
    verify(systemPromptService).discardVersion(createdVersionIds.get(1));
    verify(systemPromptService, never()).discardVersion(createdVersionIds.get(0));
    verify(runService, never()).failRun(any(), anyString());
  }

  @Test
  void marksRunFailedWhenLlmReturnsNoChoices() {
    final UUID promptId = UUID.randomUUID();
    final UUID datasetId = UUID.randomUUID();
    final Run run = run(UUID.randomUUID(), promptId, datasetId, false, 0);

    when(runRepository.findById(run.getId())).thenReturn(Optional.of(run));
    when(runService.markRunning(any())).thenReturn(run);
    when(runService.failRun(any(), anyString())).thenReturn(run);
    when(systemPromptRepository.findById(promptId)).thenReturn(Optional.of(prompt(promptId, "P1")));
    when(datasetRepository.findActiveByIdWithItems(datasetId))
        .thenReturn(Optional.of(datasetWithItems(datasetId, 1)));

    openAiClient.failGeneration = true;

    service.executeAsync(run.getId());

    verify(runService).failRun(eq(run.getId()), anyString());
    verify(runService, never()).completeRun(any(), any(), any(), any(), any(), any());
  }

  // ---- helpers ----

  private Run run(
      final UUID id,
      final UUID promptId,
      final UUID datasetId,
      final boolean autoImprove,
      final int attempt) {
    return Run.builder()
        .id(id)
        .systemPromptId(promptId)
        .datasetId(datasetId)
        .status(RunStatus.QUEUED)
        .automaticImprovementEnabled(autoImprove)
        .automaticImprovementAttempt(attempt)
        .itemResults(new ArrayList<>())
        .build();
  }

  private SystemPrompt prompt(final UUID id, final String content) {
    return prompt(id, UUID.randomUUID(), 1, content);
  }

  private SystemPrompt prompt(
      final UUID id, final UUID familyId, final int version, final String content) {
    return SystemPrompt.builder()
        .id(id)
        .familyId(familyId)
        .versionNumber(version)
        .name("Prompt")
        .content(content)
        .build();
  }

  private Dataset datasetWithItems(final UUID datasetId, final int count) {
    final List<DatasetItem> items = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      items.add(
          DatasetItem.builder()
              .id(UUID.randomUUID())
              .position(index)
              .input("input-" + index)
              .expectedOutput("expected-" + index)
              .build());
    }
    return Dataset.builder().id(datasetId).name("Dataset").items(items).build();
  }

  /**
   * Routes by the system instruction so a single fake can serve all four call kinds. Prompt grading
   * is detected first because it is the only instruction mentioning a suggestion.
   */
  private static final class FakeOpenAiClient implements OpenAiClient {

    private String outputGrade = "{\"score\": 100, \"feedback\": \"ok\"}";
    private final Deque<String> promptGrades = new ArrayDeque<>();
    private final Deque<String> improvedPrompts = new ArrayDeque<>();
    private boolean failGeneration;

    @Override
    public OpenAiChatCompletionResponse createChatCompletion(
        final OpenAiChatCompletionRequest request) {
      final String instruction = request.messages().getFirst().content();
      if (instruction.contains("suggestion")) {
        return response(require(promptGrades, "prompt grade"));
      }
      if (instruction.contains("JSON object")) {
        return response(outputGrade);
      }
      if (instruction.contains("improved system prompt")) {
        return response(require(improvedPrompts, "improved prompt"));
      }
      if (failGeneration) {
        return new OpenAiChatCompletionResponse("id", "model", List.of(), null, null);
      }
      return response("OUT");
    }

    private String require(final Deque<String> queue, final String label) {
      final String value = queue.poll();
      if (value == null) {
        throw new IllegalStateException("Fake client ran out of " + label + " responses");
      }
      return value;
    }

    private OpenAiChatCompletionResponse response(final String content) {
      return new OpenAiChatCompletionResponse(
          "id",
          "model",
          List.of(new OpenAiChatChoice(0, new OpenAiChatMessage("assistant", content), "stop")),
          null,
          null);
    }
  }
}
