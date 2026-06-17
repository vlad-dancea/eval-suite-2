package com.group34.eval_suite.runs;

import static org.assertj.core.api.Assertions.assertThat;

import com.group34.eval_suite.datasets.Dataset;
import com.group34.eval_suite.datasets.DatasetItem;
import com.group34.eval_suite.datasets.DatasetRepository;
import com.group34.eval_suite.runs.dto.RunDetailResponse;
import com.group34.eval_suite.runs.entity.Run;
import com.group34.eval_suite.runs.entity.RunDatasetItemResult;
import com.group34.eval_suite.runs.entity.RunStatus;
import com.group34.eval_suite.systemprompts.SystemPrompt;
import com.group34.eval_suite.systemprompts.SystemPromptService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercises the real JPA persistence of {@code itemResults} (the {@link RunDatasetItemResult}
 * {@code @ElementCollection} with its unique constraints) and the {@code /runs/{id}} detail mapping
 * that joins results back to their dataset items. The test transaction rolls back, so it does not
 * leave data in the dev database.
 */
@SpringBootTest
@Transactional
class RunServicePersistenceTest {

  @Autowired private RunService runService;
  @Autowired private SystemPromptService systemPromptService;
  @Autowired private DatasetRepository datasetRepository;
  @Autowired private RunRepository runRepository;

  @Test
  void completeRunPersistsItemResultsAndDetailResolvesDatasetInputs() {
    final SystemPrompt prompt = systemPromptService.createPrompt("Prompt", "Be helpful", null);
    final UUID itemId = UUID.randomUUID();
    final Dataset dataset = saveDataset(itemId, "What is 2+2?", "4");

    final Run run = runService.createRun(prompt.getId(), dataset.getId(), false);
    assertThat(run.getStatus()).isEqualTo(RunStatus.QUEUED);

    final RunDatasetItemResult itemResult =
        RunDatasetItemResult.builder()
            .datasetItemId(itemId)
            .position(0)
            .modelOutput("4")
            .outputScore(95)
            .judgeFeedback("correct")
            .build();

    runService.completeRun(run.getId(), 95, List.of(itemResult), 80, "good prompt", "add examples");

    // Reload from the database to confirm the element-collection rows round-trip.
    final Run reloaded = runRepository.findById(run.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(RunStatus.SUCCEEDED);
    assertThat(reloaded.getOutputScore()).isEqualTo(95);
    assertThat(reloaded.getSystemPromptScore()).isEqualTo(80);
    assertThat(reloaded.getItemResults()).hasSize(1);

    final RunDetailResponse detail = runService.getRunDetail(run.getId());
    assertThat(detail.run().systemPromptFamilyId()).isEqualTo(prompt.getFamilyId());
    assertThat(detail.run().promptVersionActive()).isTrue();
    assertThat(detail.items()).hasSize(1);

    final RunDetailResponse.RunItemResultResponse item = detail.items().getFirst();
    assertThat(item.input()).isEqualTo("What is 2+2?");
    assertThat(item.expectedOutput()).isEqualTo("4");
    assertThat(item.modelOutput()).isEqualTo("4");
    assertThat(item.outputScore()).isEqualTo(95);
    assertThat(item.judgeFeedback()).isEqualTo("correct");
  }

  private Dataset saveDataset(final UUID itemId, final String input, final String expectedOutput) {
    final DatasetItem item =
        DatasetItem.builder()
            .id(itemId)
            .position(0)
            .input(input)
            .expectedOutput(expectedOutput)
            .build();
    final Dataset dataset =
        Dataset.builder()
            .id(UUID.randomUUID())
            .name("Dataset")
            .createdAt(OffsetDateTime.now())
            .items(new ArrayList<>(List.of(item)))
            .build();
    return datasetRepository.save(dataset);
  }
}
