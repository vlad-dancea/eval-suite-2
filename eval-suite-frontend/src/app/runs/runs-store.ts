import { DestroyRef, Service, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { Run, RunEvent, RunRequest, RunSummary, RunsApi } from './runs-api';

@Service()
export class RunsStore {
  private readonly api = inject(RunsApi);
  private readonly destroyRef = inject(DestroyRef);
  private eventSource: EventSource | null = null;
  private pollingInterval: number | null = null;

  private readonly activeRunsResource = httpResource<RunSummary[]>(
    () => ({
      url: this.api.activeRuns(),
    }),
    { defaultValue: [] },
  );

  readonly activeRuns = computed(() => this.activeRunsResource.value());
  readonly isLoadingActiveRuns = computed(() => this.activeRunsResource.isLoading());
  readonly activeRunsError = computed(() => this.activeRunsResource.error());

  readonly selectedRunId = signal<string | null>(null);

  private readonly detailResource = httpResource<Run | undefined>(
    () => {
      const id = this.selectedRunId();
      if (!id) {
        return undefined;
      }

      return {
        url: this.api.runDetail(id),
      };
    },
    { defaultValue: undefined },
  );

  readonly selectedRun = computed(() => this.detailResource.value());
  readonly isLoadingDetail = computed(() => this.detailResource.isLoading());
  readonly detailError = computed(() => this.detailResource.error());

  readonly isCreating = signal(false);
  readonly createError = signal<string | null>(null);
  readonly isDeleting = signal(false);
  readonly deleteError = signal<string | null>(null);
  readonly liveUpdatesConnected = signal(false);

  constructor() {
    this.connectLiveUpdates();
    this.startPollingFallback();
    this.destroyRef.onDestroy(() => this.closeLiveUpdates());
    this.destroyRef.onDestroy(() => this.stopPollingFallback());
  }

  reloadActiveRuns(): void {
    this.activeRunsResource.reload();
  }

  reloadDetail(): void {
    this.detailResource.reload();
  }

  selectRun(id: string | null): void {
    this.selectedRunId.set(id);
  }

  clearCreateError(): void {
    this.createError.set(null);
  }

  createRun(request: RunRequest): Observable<Run> {
    this.isCreating.set(true);
    this.createError.set(null);

    return this.api.createRun(request).pipe(
      tap((run) => {
        this.isCreating.set(false);
        this.selectRun(run.id);
        this.reloadActiveRuns();
      }),
      catchError((err) => {
        this.createError.set(this.errorMessage(err, 'Failed to create run.'));
        this.isCreating.set(false);
        throw err;
      }),
    );
  }

  deleteRun(id: string): Observable<void> {
    this.isDeleting.set(true);
    this.deleteError.set(null);

    return this.api.deleteRun(id).pipe(
      tap(() => {
        this.isDeleting.set(false);
        this.reloadActiveRuns();
        if (this.selectedRunId() === id) {
          this.selectRun(null);
        }
      }),
      catchError((err) => {
        this.deleteError.set(this.errorMessage(err, 'Failed to delete run.'));
        this.isDeleting.set(false);
        throw err;
      }),
    );
  }

  private connectLiveUpdates(): void {
    if (typeof EventSource === 'undefined') {
      return;
    }

    this.eventSource = new EventSource(this.api.events());
    this.eventSource.onopen = () => this.liveUpdatesConnected.set(true);
    this.eventSource.onerror = () => this.liveUpdatesConnected.set(false);

    for (const eventName of ['run.created', 'run.updated', 'run.deleted'] as const) {
      this.eventSource.addEventListener(eventName, (event) => {
        const runEvent = this.parseRunEvent(event);
        if (!runEvent) {
          return;
        }

        this.reloadActiveRuns();
        if (this.selectedRunId() === runEvent.runId) {
          this.reloadDetail();
        }
      });
    }
  }

  private closeLiveUpdates(): void {
    this.eventSource?.close();
    this.eventSource = null;
  }

  private startPollingFallback(): void {
    if (typeof window === 'undefined') {
      return;
    }

    this.pollingInterval = window.setInterval(() => {
      if (!this.hasPendingRun()) {
        return;
      }

      this.reloadActiveRuns();
      if (this.selectedRunId()) {
        this.reloadDetail();
      }
    }, 4000);
  }

  private stopPollingFallback(): void {
    if (this.pollingInterval === null || typeof window === 'undefined') {
      return;
    }

    window.clearInterval(this.pollingInterval);
    this.pollingInterval = null;
  }

  private hasPendingRun(): boolean {
    const selectedRun = this.selectedRun();
    return (
      this.activeRuns().some((run) => run.status === 'QUEUED' || run.status === 'RUNNING') ||
      selectedRun?.status === 'QUEUED' ||
      selectedRun?.status === 'RUNNING'
    );
  }

  private parseRunEvent(event: Event): RunEvent | null {
    if (!(event instanceof MessageEvent) || typeof event.data !== 'string') {
      return null;
    }

    try {
      return JSON.parse(event.data) as RunEvent;
    } catch {
      return null;
    }
  }

  private errorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Could not connect to the backend server. Please make sure it is running and try again.';
      }

      if (typeof error.error?.message === 'string') {
        return error.error.message;
      }

      if (error.status >= 500) {
        return 'The backend server could not process the run. Please try again in a moment.';
      }
    }

    return fallback;
  }
}
