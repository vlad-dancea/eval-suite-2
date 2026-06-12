import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideArchive,
  lucideCircleCheck,
  lucideClock3,
  lucideEye,
  lucideLoaderCircle,
  lucidePlay,
  lucideRefreshCw,
  lucideRadio,
  lucideTriangleAlert,
} from '@ng-icons/lucide';

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardButtonComponent } from '@/shared/button';
import { ZardCardComponent } from '@/shared/card';
import { ZardDividerComponent } from '@/shared/divider';

import { RunStatus, RunSummary } from './runs-api';
import { RunsStore } from './runs-store';
import { RunCreateModal } from './modals/create-run-modal';
import { RunDeleteModal } from './modals/delete-run-modal';

@Component({
  selector: 'evl-runs',
  imports: [
    CommonModule,
    NgIcon,
    RunCreateModal,
    RunDeleteModal,
    ZardBadgeComponent,
    ZardButtonComponent,
    ZardCardComponent,
    ZardDividerComponent,
  ],
  providers: [
    provideIcons({
      lucideArchive,
      lucideCircleCheck,
      lucideClock3,
      lucideEye,
      lucideLoaderCircle,
      lucidePlay,
      lucideRadio,
      lucideRefreshCw,
      lucideTriangleAlert,
    }),
  ],
  templateUrl: './runs.component.html',
})
export class RunsComponent {
  protected readonly store = inject(RunsStore);
  protected readonly showCreateModal = signal(false);
  protected readonly showDeleteModal = signal(false);
  protected readonly selectedRunForDelete = signal<RunSummary | null>(null);

  protected readonly selectedRunSummary = computed(() => {
    const selectedId = this.store.selectedRunId();
    return this.store.activeRuns().find((run) => run.id === selectedId) ?? null;
  });

  openCreateModal(): void {
    this.store.clearCreateError();
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  selectRun(run: RunSummary): void {
    this.store.selectRun(run.id);
  }

  openDeleteModal(run: RunSummary): void {
    this.selectedRunForDelete.set(run);
    this.showDeleteModal.set(true);
  }

  closeDeleteModal(): void {
    this.showDeleteModal.set(false);
    this.selectedRunForDelete.set(null);
  }

  confirmDeleteRun(): void {
    const run = this.selectedRunForDelete();
    if (!run) {
      return;
    }

    this.store.deleteRun(run.id).subscribe({
      next: () => this.closeDeleteModal(),
      error: (err) => console.error(err),
    });
  }

  statusLabel(status: RunStatus): string {
    switch (status) {
      case 'QUEUED':
        return 'Queued';
      case 'RUNNING':
        return 'Running';
      case 'SUCCEEDED':
        return 'Succeeded';
      case 'FAILED':
        return 'Failed';
    }
  }

  statusBadgeClass(status: RunStatus): string {
    switch (status) {
      case 'QUEUED':
        return 'border-muted-foreground/30 bg-muted text-muted-foreground';
      case 'RUNNING':
        return 'border-chart-2/40 bg-chart-2/10 text-chart-2';
      case 'SUCCEEDED':
        return 'border-chart-4/40 bg-chart-4/10 text-foreground';
      case 'FAILED':
        return 'border-destructive/40 bg-destructive/10 text-destructive';
    }
  }

  statusIcon(status: RunStatus): string {
    switch (status) {
      case 'QUEUED':
        return 'lucideClock3';
      case 'RUNNING':
        return 'lucideLoaderCircle';
      case 'SUCCEEDED':
        return 'lucideCircleCheck';
      case 'FAILED':
        return 'lucideTriangleAlert';
    }
  }

  scoreLabel(score: number | null): string {
    return score === null ? 'Pending' : `${Math.round(score)}%`;
  }
}
