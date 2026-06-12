import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type RunStatus = 'QUEUED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED';

export interface RunItem {
  id: string;
  datasetItemId: string;
  position: number;
  input: string;
  expectedOutput: string;
  modelOutput: string | null;
  outputScore: number | null;
  promptScore: number | null;
  judgeExplanation: string | null;
  improvementSuggestion: string | null;
  status: RunStatus;
  errorMessage: string | null;
  createdAt: string;
  completedAt: string | null;
}

export interface RunSummary {
  id: string;
  systemPromptName: string;
  systemPromptVersionNumber: number;
  datasetName: string;
  datasetItemCount: number;
  status: RunStatus;
  averageOutputScore: number | null;
  averagePromptScore: number | null;
  errorMessage: string | null;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
}

export interface Run extends RunSummary {
  systemPromptId: string;
  systemPromptFamilyId: string;
  systemPromptContent: string;
  datasetId: string;
  deletedAt: string | null;
  items: RunItem[];
}

export interface RunRequest {
  systemPromptId: string;
  datasetId: string;
}

export interface RunEvent {
  type: 'run.created' | 'run.updated' | 'run.deleted';
  runId: string;
  run: RunSummary;
}

@Service()
export class RunsApi {
  private readonly baseUrl = 'http://localhost:8080/api/runs';
  private readonly http = inject(HttpClient);

  activeRuns(): string {
    return this.baseUrl;
  }

  runDetail(id: string): string {
    return `${this.baseUrl}/${id}`;
  }

  events(): string {
    return `${this.baseUrl}/events`;
  }

  createRun(request: RunRequest): Observable<Run> {
    return this.http.post<Run>(this.baseUrl, request);
  }

  deleteRun(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
