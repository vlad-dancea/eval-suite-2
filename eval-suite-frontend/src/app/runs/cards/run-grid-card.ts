import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardCardComponent } from '@/shared/card';

import { type Run, RunStatus } from '../runs.api';

@Component({
  selector: 'evl-run-grid-card',
  imports: [DatePipe, ZardBadgeComponent, ZardCardComponent],
  template: `
    <z-card class="group min-h-36 justify-between">
      <div class="space-y-5">
        <div class="flex items-start justify-between gap-3">
          <z-badge [zType]="statusBadgeType()" class="h-5 px-1.5 text-xs font-medium">
            {{ statusLabel() }}
          </z-badge>

          <time class="shrink-0 text-xs text-muted-foreground">
            {{ run().createdAt | date: 'short' }}
          </time>
        </div>

        <div class="space-y-2.5">
          <div class="flex min-w-0 items-center gap-2">
            <h2
              class="truncate text-lg font-semibold leading-6 text-foreground"
              [title]="run().systemPromptName">
              {{ run().systemPromptName }}
            </h2>

            @if (run().systemPromptVersionNumber !== null) {
              <span
                class="shrink-0 rounded-md border bg-muted/40 px-1.5 py-0.5 text-xs font-medium text-muted-foreground">
                v{{ run().systemPromptVersionNumber }}
              </span>
            }
          </div>

          <div class="flex min-w-0 items-center gap-2 text-muted-foreground">
            <span class="h-px w-6 shrink-0 bg-border"></span>
            <p class="truncate text-sm leading-5" [title]="run().datasetName">
              {{ run().datasetName }}
            </p>
          </div>
        </div>
      </div>
    </z-card>
  `,
})
export class RunGridCard {
  readonly run = input.required<Run>();

  protected readonly statusLabel = computed(() => {
    switch (this.run().status) {
      case RunStatus.QUEUED:
        return 'Queued';
      case RunStatus.RUNNING:
        return 'Running';
      case RunStatus.SUCCEEDED:
        return 'Succeeded';
      case RunStatus.FAILED:
        return 'Failed';
    }
  });

  protected readonly statusBadgeType = computed(() => {
    switch (this.run().status) {
      case RunStatus.QUEUED:
        return 'secondary';
      case RunStatus.RUNNING:
        return 'outline';
      case RunStatus.SUCCEEDED:
        return 'default';
      case RunStatus.FAILED:
        return 'destructive';
    }
  });
}
