package com.group34.eval_suite.datasets;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@SuppressWarnings({"PMD.ShortVariable", "PMD.CyclomaticComplexity", "PMD.LongVariable"})
public class DatasetService {

  private static final int MAX_CONTENT_LENGTH = 5000;
  private static final int MAX_NAME_LENGTH = 120;
  private static final int MAX_ITEMS = 500;

  private final DatasetRepository repository;

  public DatasetService(DatasetRepository repository) {
    this.repository = repository;
  }

  /** Create a new immutable dataset together with its input/expected-output pairs. */
  @Transactional
  public Dataset createDataset(final String name, final List<DatasetItemRequest> items) {
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty");
    }
    if (name.length() > MAX_NAME_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Name cannot exceed " + MAX_NAME_LENGTH + " characters");
    }
    if (items == null || items.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A dataset must contain at least one item");
    }
    if (items.size() > MAX_ITEMS) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A dataset cannot contain more than " + MAX_ITEMS + " items");
    }

    final Dataset dataset =
        Dataset.builder()
            .id(UUID.randomUUID())
            .name(name)
            .createdAt(OffsetDateTime.now())
            .deletedAt(null)
            .build();

    int position = 0;
    for (final DatasetItemRequest item : items) {
      dataset.getItems().add(buildItem(item, position));
      position++;
    }

    return repository.save(dataset);
  }

  private DatasetItem buildItem(final DatasetItemRequest item, final int position) {
    final String input = item.input();
    final String expectedOutput = item.expectedOutput();
    if (input == null || input.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Input cannot be empty (item " + (position + 1) + ")");
    }
    if (input.length() > MAX_CONTENT_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Input cannot exceed "
              + MAX_CONTENT_LENGTH
              + " characters (item "
              + (position + 1)
              + ")");
    }
    if (expectedOutput == null || expectedOutput.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Expected output cannot be empty (item " + (position + 1) + ")");
    }
    if (expectedOutput.length() > MAX_CONTENT_LENGTH) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Expected output cannot exceed "
              + MAX_CONTENT_LENGTH
              + " characters (item "
              + (position + 1)
              + ")");
    }

    return DatasetItem.builder()
        .id(UUID.randomUUID())
        .position(position)
        .input(input)
        .expectedOutput(expectedOutput)
        .build();
  }

  /** Get all active datasets as lightweight summaries. */
  public List<DatasetSummaryResponse> getActiveDatasets() {
    return repository.findActiveSummaries();
  }

  /** Get a single active dataset with all of its items. */
  @Transactional(readOnly = true)
  public Dataset getDatasetById(final UUID id) {
    return repository
        .findActiveByIdWithItems(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));
  }

  /** Soft-delete a dataset by setting its deleted_at timestamp. */
  @Transactional
  public Dataset deleteDataset(final UUID id) {
    final Dataset dataset =
        repository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found"));
    if (dataset.getDeletedAt() == null) {
      dataset.setDeletedAt(OffsetDateTime.now());
    }
    return dataset;
  }
}
