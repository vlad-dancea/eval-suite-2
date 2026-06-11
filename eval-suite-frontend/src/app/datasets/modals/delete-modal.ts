import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideArchive, lucideTriangleAlert, lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';

import { type DatasetSummary } from '../datasets-api';

@Component({
  selector: 'evl-dataset-delete-modal',
  imports: [NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucideArchive, lucideTriangleAlert, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="deleteDatasetModalTitle"
        aria-describedby="deleteDatasetModalDescription"
        class="flex w-full max-w-lg flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="min-w-0 space-y-1">
            <h2 id="deleteDatasetModalTitle" class="text-lg font-semibold text-foreground">
              Archive this dataset?
            </h2>

            <p class="truncate text-sm text-muted-foreground" [title]="dataset().name">
              {{ dataset().name }}
            </p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" aria-hidden="true" />
            Close
          </button>
        </header>

        <div id="deleteDatasetModalDescription" class="space-y-4 px-7 py-6 text-sm leading-6">
          <div
            class="flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4">
            <ng-icon
              name="lucideTriangleAlert"
              class="mt-0.5 h-4 w-4 shrink-0 text-destructive"
              aria-hidden="true" />

            <p class="text-foreground">
              This dataset will no longer be available for use in new runs.
            </p>
          </div>

          <p class="text-xs leading-5 text-muted-foreground">
            Existing runs that already used this dataset are not affected.
          </p>

          <p class="font-medium text-foreground">Do you want to continue?</p>
        </div>

        <footer class="flex justify-end gap-2 border-t px-7 py-4">
          <button z-button zType="outline" type="button" (click)="dismissed.emit()">Cancel</button>

          <button z-button zType="destructive" type="button" (click)="confirmed.emit()">
            <ng-icon name="lucideArchive" aria-hidden="true" />
            Archive
          </button>
        </footer>
      </section>
    </div>
  `,
})
export class DatasetDeleteModal {
  readonly dataset = input.required<DatasetSummary>();
  readonly confirmed = output<void>();
  readonly dismissed = output<void>();
}
