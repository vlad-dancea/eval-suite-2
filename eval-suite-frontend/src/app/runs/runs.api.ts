import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface CreateRunRequest {
  systemPromptId: string;
  datasetId: string;
  automaticImprovementEnabled?: boolean;
}

export interface Run {
  id: string;
  systemPromptId: string;
  datasetId: string;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  status: RunStatus;
  outputScore: number | null;
  systemPromptScore: number | null;
  systemPromptFeedback: string | null;
  systemPromptImprovementSuggestion: string | null;
  automaticImprovementEnabled: boolean;
  automaticImprovementAttempt: number | null;
  failureMessage: string | null;
}

export enum RunStatus {
  QUEUED = 'QUEUED',
  RUNNING = 'RUNNING',
  SUCCEEDED = 'SUCCEEDED',
  FAILED = 'FAILED',
}

export type RunEvent =
  | {
      type: 'RUN_CREATED';
      run: Run;
    }
  | {
      type: 'RUN_UPDATED';
      run: Run;
    }
  | {
      type: 'RUN_COMPLETED';
      runId: string;
    }
  | {
      type: 'RUN_FAILED';
      runId: string;
    };

@Service()
export class RunsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/runs';
  private readonly eventsUrl = `${this.baseUrl}/events`;

  connect(): Observable<RunEvent> {
    return new Observable<RunEvent>((subscriber) => {
      const source = new EventSource(this.eventsUrl, {
        withCredentials: true,
      });

      const onMessage = (message: MessageEvent<string>) => {
        try {
          subscriber.next(JSON.parse(message.data) as RunEvent);
        } catch (error) {
          subscriber.error(error);
        }
      };

      source.addEventListener('RUN_CREATED', onMessage);
      source.addEventListener('RUN_COMPLETED', onMessage);
      source.addEventListener('RUN_FAILED', onMessage);
      source.addEventListener('error', () => {});

      return () => {
        source.removeEventListener('RUN_CREATED', onMessage);
        source.removeEventListener('RUN_COMPLETED', onMessage);
        source.removeEventListener('RUN_FAILED', onMessage);
        source.close();
      };
    });
  }

  runs(): string {
    return this.baseUrl;
  }

  runById(id: string): string {
    return `${this.baseUrl}/${id}`;
  }

  createRun(runRequest: CreateRunRequest): Observable<Run> {
    return this.http.post<Run>(this.baseUrl, runRequest);
  }
}
