import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SystemPrompt {
  id: string;
  familyId: string;
  versionNumber: number;
  name: string;
  content: string;
  preview: string;
  createdAt: string;
  deletedAt: string | null;
}

export interface SystemPromptRequest {
  name: string;
  content: string;
  familyId?: string;
}

@Service()
export class SystemPromptsApi {
  private readonly baseUrl = 'http://localhost:8080/api/system-prompts';
  private readonly http = inject(HttpClient);

  activePrompts(): string {
    return this.baseUrl;
  }

  familyHistory(id: string): string {
    return `${this.baseUrl}/families/${id}/versions`;
  }

  createPrompt(request: SystemPromptRequest): Observable<SystemPrompt> {
    return this.http.post<SystemPrompt>(this.baseUrl, request);
  }

  archivePrompt(id: string): Observable<SystemPrompt> {
    return this.http.delete<SystemPrompt>(`${this.baseUrl}/${id}`);
  }
}
