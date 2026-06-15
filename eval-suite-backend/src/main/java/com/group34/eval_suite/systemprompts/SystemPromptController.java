package com.group34.eval_suite.systemprompts;

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
@RequestMapping("/api/system-prompts")
@CrossOrigin(origins = "*") // Allow requests from all origins (e.g. frontend dev server)
@SuppressWarnings("PMD.ShortVariable")
public class SystemPromptController {

  private final SystemPromptService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SystemPromptResponse createPrompt(@Valid @RequestBody SystemPromptRequest request) {
    final SystemPrompt prompt =
        service.createPrompt(request.name(), request.content(), request.familyId());
    return SystemPromptResponse.fromEntity(prompt);
  }

  @GetMapping
  public List<SystemPromptResponse> getActivePrompts() {
    return service.getActivePrompts().stream().map(SystemPromptResponse::fromEntity).toList();
  }

  @GetMapping("/{id}")
  public SystemPromptResponse getPromptById(@PathVariable("id") UUID id) {
    final SystemPrompt prompt = service.getPromptById(id);
    return SystemPromptResponse.fromEntity(prompt);
  }

  @GetMapping("/families/{familyId}/versions")
  public List<SystemPromptResponse> getFamilyHistory(@PathVariable("familyId") UUID familyId) {
    return service.getFamilyHistory(familyId).stream()
        .map(SystemPromptResponse::fromEntity)
        .toList();
  }

  @DeleteMapping("/{id}")
  public SystemPromptResponse archivePrompt(@PathVariable("id") UUID id) {
    final SystemPrompt prompt = service.archivePrompt(id);
    return SystemPromptResponse.fromEntity(prompt);
  }
}
