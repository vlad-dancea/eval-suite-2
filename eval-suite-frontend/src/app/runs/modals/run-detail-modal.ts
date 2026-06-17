import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideX } from '@ng-icons/lucide';

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardButtonComponent } from '@/shared/button';

import { RunStatus } from '../runs.api';
import { RunsStore } from '../runs.store';

@Component({
  selector: 'evl-run-detail-modal',
  imports: [DatePipe, NgIcon, ZardBadgeComponent, ZardButtonComponent],
  providers: [provideIcons({ lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="runDetailModalTitle"
        class="flex max-h-[88vh] w-full max-w-4xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="space-y-1">
            <h2 id="runDetailModalTitle" class="text-lg font-semibold text-foreground">
              Run history
            </h2>
            <p class="text-sm text-muted-foreground">{{ familyName() }}</p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" class="h-4 w-4" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="grid min-h-0 flex-1 grid-cols-1 sm:grid-cols-[260px_1fr]">
          <!-- Timeline -->
          <nav
            class="max-h-72 overflow-y-auto border-b p-3 sm:max-h-none sm:border-b-0 sm:border-r"
            aria-label="Runs in this prompt family">
            <ul class="space-y-1">
              @for (run of store.familyRuns(); track run.id) {
                <li>
                  <button
                    type="button"
                    class="flex w-full flex-col gap-1 rounded-md px-3 py-2 text-left outline-none hover:bg-accent focus-visible:bg-accent"
                    [class.bg-accent]="store.selectedRunId() === run.id"
                    (click)="store.selectRun(run.id)">
                    <span class="flex items-center justify-between gap-2">
                      <span class="text-sm font-medium text-foreground">
                        v{{ run.systemPromptVersionNumber }}
                        @if (!run.promptVersionActive) {
                          <span class="text-xs font-normal text-muted-foreground">(discarded)</span>
                        }
                      </span>
                      <z-badge [zType]="statusBadgeType(run.status)" class="h-5 px-1.5 text-[10px]">
                        {{ statusLabel(run.status) }}
                      </z-badge>
                    </span>
                    <span class="text-xs text-muted-foreground">
                      @if (run.systemPromptScore !== null) {
                        Prompt {{ run.systemPromptScore }}/100 ·
                      }
                      {{ run.createdAt | date: 'short' }}
                    </span>
                  </button>
                </li>
              }
            </ul>
          </nav>

          <!-- Drill-in -->
          <div class="min-h-72 flex-1 overflow-y-auto p-6">
            @if (!store.selectedRunId()) {
              <div class="flex h-full min-h-48 items-center justify-center text-center">
                <p class="text-sm text-muted-foreground">Select a run to see its results.</p>
              </div>
            } @else if (store.isLoadingDetail()) {
              <div class="flex min-h-48 flex-col items-center justify-center">
                <div
                  class="h-8 w-8 animate-spin rounded-full border-2 border-muted border-t-foreground"></div>
                <p class="mt-4 text-sm text-muted-foreground">Loading results...</p>
              </div>
            } @else if (store.detailError()) {
              <p class="text-sm text-destructive">Could not load this run's details.</p>
            } @else if (store.selectedRunDetail(); as detail) {
              <div class="space-y-6">
                <div class="space-y-2">
                  <div class="flex flex-wrap items-center gap-2">
                    <z-badge
                      [zType]="statusBadgeType(detail.run.status)"
                      class="h-5 px-1.5 text-xs">
                      {{ statusLabel(detail.run.status) }}
                    </z-badge>
                    @if (detail.run.systemPromptScore !== null) {
                      <span class="rounded-md border bg-muted/40 px-2 py-0.5 text-xs font-medium">
                        Prompt {{ detail.run.systemPromptScore }}/100
                      </span>
                    }
                    @if (detail.run.outputScore !== null) {
                      <span
                        class="rounded-md border bg-muted/40 px-2 py-0.5 text-xs font-medium text-muted-foreground">
                        Output {{ detail.run.outputScore }}/100
                      </span>
                    }
                  </div>

                  @if (detail.run.failureMessage) {
                    <p class="text-sm text-destructive">{{ detail.run.failureMessage }}</p>
                  }
                  @if (detail.run.systemPromptFeedback) {
                    <div class="rounded-lg border bg-muted/30 p-3">
                      <p class="text-xs font-medium text-foreground">Judge feedback</p>
                      <p class="mt-1 text-sm leading-5 text-muted-foreground">
                        {{ detail.run.systemPromptFeedback }}
                      </p>
                    </div>
                  }
                  @if (detail.run.systemPromptImprovementSuggestion) {
                    <div class="rounded-lg border bg-muted/30 p-3">
                      <p class="text-xs font-medium text-foreground">Improvement suggestion</p>
                      <p class="mt-1 text-sm leading-5 text-muted-foreground">
                        {{ detail.run.systemPromptImprovementSuggestion }}
                      </p>
                    </div>
                  }
                </div>

                @if (detail.items.length > 0) {
                  <div class="space-y-4">
                    <h3 class="text-sm font-semibold text-foreground">Per-item results</h3>
                    @for (item of detail.items; track item.datasetItemId) {
                      <article class="space-y-2 rounded-lg border p-4">
                        <div class="flex items-center justify-between">
                          <span class="text-xs font-medium text-muted-foreground">
                            Item {{ (item.position ?? 0) + 1 }}
                          </span>
                          @if (item.outputScore !== null) {
                            <span
                              class="rounded-md border bg-muted/40 px-2 py-0.5 text-xs font-medium">
                              {{ item.outputScore }}/100
                            </span>
                          }
                        </div>

                        <div class="grid gap-3 sm:grid-cols-2">
                          <div>
                            <p class="text-xs font-medium text-foreground">Input</p>
                            <p
                              class="mt-1 whitespace-pre-wrap break-words font-mono text-xs text-muted-foreground">
                              {{ item.input }}
                            </p>
                          </div>
                          <div>
                            <p class="text-xs font-medium text-foreground">Expected output</p>
                            <p
                              class="mt-1 whitespace-pre-wrap break-words font-mono text-xs text-muted-foreground">
                              {{ item.expectedOutput }}
                            </p>
                          </div>
                        </div>

                        <div>
                          <p class="text-xs font-medium text-foreground">Actual output</p>
                          <p
                            class="mt-1 whitespace-pre-wrap break-words font-mono text-xs text-muted-foreground">
                            {{ item.modelOutput }}
                          </p>
                        </div>

                        @if (item.judgeFeedback) {
                          <div>
                            <p class="text-xs font-medium text-foreground">Judge feedback</p>
                            <p class="mt-1 text-xs leading-5 text-muted-foreground">
                              {{ item.judgeFeedback }}
                            </p>
                          </div>
                        }
                      </article>
                    }
                  </div>
                } @else {
                  <p class="text-sm text-muted-foreground">
                    No per-item results yet. They appear once the run finishes.
                  </p>
                }
              </div>
            }
          </div>
        </div>
      </section>
    </div>
  `,
})
export class RunDetailModal {
  readonly familyName = input.required<string>();
  readonly dismissed = output<void>();

  protected readonly store = inject(RunsStore);

  protected statusLabel(status: RunStatus): string {
    switch (status) {
      case RunStatus.QUEUED:
        return 'Queued';
      case RunStatus.RUNNING:
        return 'Running';
      case RunStatus.SUCCEEDED:
        return 'Succeeded';
      case RunStatus.FAILED:
        return 'Failed';
    }
  }

  protected statusBadgeType(
    status: RunStatus,
  ): 'secondary' | 'outline' | 'default' | 'destructive' {
    switch (status) {
      case RunStatus.QUEUED:
        return 'secondary';
      case RunStatus.RUNNING:
        return 'outline';
      case RunStatus.SUCCEEDED:
        return 'default';
      case RunStatus.FAILED:
        return 'destructive';
    }
  }
}
