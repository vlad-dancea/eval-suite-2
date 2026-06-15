import { CreateRunRequest, Run, RunEvent, RunsApi, RunStatus } from '@/runs/runs.api';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { computed, DestroyRef, inject, Service, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Service()
export class RunsStore {
  private readonly api = inject(RunsApi);
  private readonly destroyRef = inject(DestroyRef);

  private readonly runsResource = httpResource<Run[]>(
    () => ({
      url: this.api.runs(),
    }),
    {
      defaultValue: [],
    },
  );

  private readonly patchedRunsById = signal<Record<string, Run>>({});

  readonly runs = computed(() => {
    const merged = new Map<string, Run>();

    for (const run of this.runsResource.value()) {
      merged.set(run.id, run);
    }

    for (const run of Object.values(this.patchedRunsById())) {
      merged.set(run.id, run);
    }

    return Array.from(merged.values()).sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  });

  readonly isLoadingRuns = computed(() => this.runsResource.isLoading());
  readonly runsError = computed(() => this.runsResource.error());

  readonly queuedRuns = computed(() =>
    this.runs().filter((run) => run.status === RunStatus.QUEUED),
  );

  readonly runningRuns = computed(() =>
    this.runs().filter((run) => run.status === RunStatus.RUNNING),
  );

  readonly finishedRuns = computed(() =>
    this.runs().filter(
      (run) => run.status === RunStatus.SUCCEEDED || run.status === RunStatus.FAILED,
    ),
  );

  readonly stats = computed(() => {
    const runs = this.runs();

    return {
      total: runs.length,
      queued: runs.filter((run) => run.status === RunStatus.QUEUED).length,
      running: runs.filter((run) => run.status === RunStatus.RUNNING).length,
      succeeded: runs.filter((run) => run.status === RunStatus.SUCCEEDED).length,
      failed: runs.filter((run) => run.status === RunStatus.FAILED).length,
    };
  });

  readonly lastEvent = signal<RunEvent | null>(null);
  readonly hasReceivedLiveEvent = signal(false);
  readonly isCreating = signal(false);
  readonly createError = signal<string | null>(null);

  constructor() {
    this.connectToRunEvents();
  }

  reloadRuns(): void {
    this.runsResource.reload();
  }

  clearCreateError(): void {
    this.createError.set(null);
  }

  createRun(request: CreateRunRequest): Observable<Run> {
    this.isCreating.set(true);
    this.createError.set(null);

    return this.api.createRun(request).pipe(
      tap((run) => {
        this.isCreating.set(false);
        this.patchedRunsById.update((current) => ({
          ...current,
          [run.id]: run,
        }));
        this.reloadRuns();
      }),
      catchError((error) => {
        this.createError.set(this.createRunErrorMessage(error));
        this.isCreating.set(false);
        throw error;
      }),
    );
  }

  private connectToRunEvents(): void {
    this.api
      .connect()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (event) => {
          this.hasReceivedLiveEvent.set(true);
          this.lastEvent.set(event);
          this.applyRunEvent(event);
        },
      });
  }

  private applyRunEvent(event: RunEvent): void {
    switch (event.type) {
      case 'RUN_CREATED':
        this.reloadRuns();
        break;

      case 'RUN_UPDATED':
        this.patchedRunsById.update((current) => ({
          ...current,
          [event.run.id]: event.run,
        }));
        break;

      case 'RUN_COMPLETED':
        this.patchRunStatus(event.runId, RunStatus.SUCCEEDED);
        break;

      case 'RUN_FAILED':
        this.patchRunStatus(event.runId, RunStatus.FAILED);
        break;
    }
  }

  private patchRunStatus(runId: string, status: RunStatus): void {
    const existingRun = this.runs().find((run) => run.id === runId);

    if (!existingRun) {
      this.reloadRuns();
      return;
    }

    this.patchedRunsById.update((current) => ({
      ...current,
      [runId]: {
        ...existingRun,
        status,
      },
    }));
  }

  private createRunErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Could not connect to the backend server. Please make sure it is running and try again.';
      }

      if (typeof error.error?.message === 'string') {
        return error.error.message;
      }

      if (error.status >= 500) {
        return 'The backend server could not create the run. Please try again in a moment.';
      }

      if (error.status >= 400) {
        return 'The run could not be started. Please check the selected prompt and dataset.';
      }
    }

    return 'Failed to create run. Please try again.';
  }
}
