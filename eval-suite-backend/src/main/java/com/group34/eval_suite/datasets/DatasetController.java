package com.group34.eval_suite.datasets;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/datasets")
@CrossOrigin(origins = "*") // Allow requests from all origins (e.g. frontend dev server)
@SuppressWarnings("PMD.ShortVariable")
public class DatasetController {

  private final DatasetService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DatasetResponse createDataset(@Valid @RequestBody DatasetRequest request) {
    final Dataset dataset = service.createDataset(request.name(), request.items());
    return DatasetResponse.fromEntity(dataset);
  }

  @GetMapping
  public List<DatasetSummaryResponse> getActiveDatasets() {
    return service.getActiveDatasets();
  }

  @GetMapping("/{id}")
  public DatasetResponse getDatasetById(@PathVariable("id") UUID id) {
    return DatasetResponse.fromEntity(service.getDatasetById(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDataset(@PathVariable("id") UUID id) {
    service.deleteDataset(id);
  }
}
