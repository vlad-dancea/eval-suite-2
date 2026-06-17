import { CreateRunRequest, Run, RunDetail, RunEvent, RunsApi, RunStatus } from '@/runs/runs.api';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { computed, DestroyRef, effect, inject, Service, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

function recencyKey(run: Run): string {
  return run.updatedAt ?? run.createdAt;
}

function byCreatedDesc(a: Run, b: Run): number {
  return b.createdAt.localeCompare(a.createdAt);
}

/**
 * Representative run for a family card: prefer a run whose prompt version is still active, then the
 * most recently created one. This keeps "most recent run" and "keep the best version" consistent —
 * discarded auto-improvement attempts are inactive and never shadow the kept best version.
 */
function isBetterRepresentative(candidate: Run, current: Run): boolean {
  if (candidate.promptVersionActive !== current.promptVersionActive) {
    return candidate.promptVersionActive;
  }
  return candidate.createdAt.localeCompare(current.createdAt) > 0;
}

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

  // Single source of truth, seeded by the server snapshot and updated live by SSE. Entries are
  // keyed by run id and the newer `updatedAt` always wins, so a reload can never resurrect a stale
  // state and a live update is never permanently shadowed by an older snapshot.
  private readonly runsById = signal<Record<string, Run>>({});

  readonly runs = computed(() => Object.values(this.runsById()).sort(byCreatedDesc));

  readonly isLoadingRuns = computed(() => this.runsResource.isLoading());
  readonly runsError = computed(() => this.runsResource.error());

  // Dashboard shows one card per prompt family (most recent run on the active version).
  readonly familyCards = computed(() => {
    const byFamily = new Map<string, Run>();
    for (const run of this.runs()) {
      const key = run.systemPromptFamilyId ?? run.id;
      const current = byFamily.get(key);
      if (!current || isBetterRepresentative(run, current)) {
        byFamily.set(key, run);
      }
    }
    return Array.from(byFamily.values()).sort(byCreatedDesc);
  });

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

  readonly isCreating = signal(false);
  readonly createError = signal<string | null>(null);

  // Run/family detail (timeline + drill-in).
  readonly selectedFamilyId = signal<string | null>(null);
  readonly selectedRunId = signal<string | null>(null);

  private readonly detailResource = httpResource<RunDetail | undefined>(
    () => {
      const id = this.selectedRunId();
      if (!id) {
        return undefined;
      }
      return { url: this.api.runById(id) };
    },
    {
      defaultValue: undefined,
    },
  );

  readonly selectedRunDetail = computed(() => this.detailResource.value());
  readonly isLoadingDetail = computed(() => this.detailResource.isLoading());
  readonly detailError = computed(() => this.detailResource.error());

  readonly familyRuns = computed(() => {
    const familyId = this.selectedFamilyId();
    if (!familyId) {
      return [];
    }
    return this.runs().filter((run) => run.systemPromptFamilyId === familyId);
  });

  constructor() {
    // Merge each server snapshot into the live map.
    effect(() => {
      this.mergeRuns(this.runsResource.value());
    });
    this.connectToRunEvents();
  }

  reloadRuns(): void {
    this.runsResource.reload();
  }

  selectFamily(familyId: string | null): void {
    this.selectedFamilyId.set(familyId);
    this.selectedRunId.set(null);
  }

  selectRun(runId: string | null): void {
    this.selectedRunId.set(runId);
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
        this.mergeRuns([run]);
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
        next: (event) => this.applyRunEvent(event),
      });
  }

  private applyRunEvent(event: RunEvent): void {
    this.mergeRuns([event.run]);
    if (event.type === 'RUN_CREATED') {
      // New runs (including auto-improvement attempts) may not be in our snapshot yet.
      this.reloadRuns();
    }
  }

  private mergeRuns(incoming: Run[]): void {
    if (incoming.length === 0) {
      return;
    }
    this.runsById.update((current) => {
      const next = { ...current };
      for (const run of incoming) {
        const existing = next[run.id];
        if (!existing || recencyKey(run) >= recencyKey(existing)) {
          next[run.id] = run;
        }
      }
      return next;
    });
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
