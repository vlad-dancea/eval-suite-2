import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideTriangleAlert, lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';

import { DatasetsStore } from '../datasets-store';

@Component({
  selector: 'evl-dataset-view-modal',
  imports: [NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucideTriangleAlert, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="datasetViewModalTitle"
        class="flex max-h-[88vh] w-full max-w-5xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="min-w-0 space-y-1">
            <h2
              id="datasetViewModalTitle"
              class="truncate text-lg font-semibold text-foreground"
              [title]="datasetName()">
              {{ datasetName() }}
            </h2>

            @if (store.selectedDataset(); as dataset) {
              <p class="text-sm text-muted-foreground">
                {{ dataset.itemCount }} {{ dataset.itemCount === 1 ? 'item' : 'items' }}
              </p>
            }
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="flex-1 overflow-y-auto px-7 py-8">
          @if (store.isLoadingDetail()) {
            <p class="text-sm text-muted-foreground">Loading dataset items…</p>
          } @else if (store.detailError()) {
            <div
              class="flex flex-col items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-6 text-center">
              <ng-icon
                name="lucideTriangleAlert"
                class="h-5 w-5 text-destructive"
                aria-hidden="true" />
              <p class="text-sm text-muted-foreground">Could not load this dataset.</p>
            </div>
          } @else if (store.selectedDataset(); as dataset) {
            <ol class="divide-y">
              @for (item of dataset.items; track item.id) {
                <li class="py-5 first:pt-0 last:pb-0">
                  <div class="mb-4 flex items-center justify-between gap-3">
                    <h3 class="text-sm font-medium text-muted-foreground">
                      Item {{ item.position + 1 }}
                    </h3>
                  </div>

                  <div class="grid grid-cols-1 gap-5 lg:grid-cols-2">
                    <section class="min-w-0 space-y-2">
                      <h3 class="text-xs font-medium uppercase text-muted-foreground">Input</h3>

                      <p
                        class="max-h-80 overflow-y-auto whitespace-pre-wrap break-words rounded-md border border-input bg-background p-4 font-mono text-xs leading-6 text-foreground">
                        {{ item.input }}
                      </p>
                    </section>

                    <section class="min-w-0 space-y-2">
                      <h3 class="text-xs font-medium uppercase text-muted-foreground">
                        Expected output
                      </h3>

                      <p
                        class="max-h-80 overflow-y-auto whitespace-pre-wrap break-words rounded-md border border-input bg-background p-4 font-mono text-xs leading-6 text-foreground">
                        {{ item.expectedOutput }}
                      </p>
                    </section>
                  </div>
                </li>
              }
            </ol>
          }
        </div>
      </section>
    </div>
  `,
})
export class DatasetViewModal {
  protected readonly store = inject(DatasetsStore);

  readonly datasetName = input.required<string>();
  readonly dismissed = output<void>();
}
