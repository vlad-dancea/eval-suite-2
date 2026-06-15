import { Run, RunEvent, RunsApi, RunStatus } from '@/runs/runs.api';
import { httpResource } from '@angular/common/http';
import { computed, DestroyRef, inject, Service, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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

  constructor() {
    this.connectToRunEvents();
  }

  reloadRuns(): void {
    this.runsResource.reload();
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
}
