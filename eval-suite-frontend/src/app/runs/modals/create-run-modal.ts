import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucidePlay, lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';
import { DatasetsStore } from '@/datasets/datasets-store';
import { SystemPromptsStore } from '@/system-prompts/system-prompts-store';

import { RunsStore } from '../runs-store';

@Component({
  selector: 'evl-run-create-modal',
  imports: [FormsModule, NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucidePlay, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="runCreateModalTitle"
        class="flex w-full max-w-2xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="space-y-1">
            <h2 id="runCreateModalTitle" class="text-lg font-semibold text-foreground">
              Create run
            </h2>
            <p class="text-sm text-muted-foreground">
              Pair an active prompt with an active dataset.
            </p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="space-y-5 px-7 py-6">
          <label class="block space-y-2">
            <span class="text-sm font-medium text-foreground">System prompt</span>
            <select
              class="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              [ngModel]="systemPromptId()"
              (ngModelChange)="systemPromptId.set($event)"
              [attr.aria-invalid]="!systemPromptId() && attemptedSubmit() ? 'true' : null">
              <option value="">Select a prompt</option>
              @for (prompt of promptsStore.activePrompts(); track prompt.id) {
                <option [value]="prompt.id">{{ prompt.name }} v{{ prompt.versionNumber }}</option>
              }
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-foreground">Dataset</span>
            <select
              class="h-10 w-full rounded-md border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              [ngModel]="datasetId()"
              (ngModelChange)="datasetId.set($event)"
              [attr.aria-invalid]="!datasetId() && attemptedSubmit() ? 'true' : null">
              <option value="">Select a dataset</option>
              @for (dataset of datasetsStore.activeDatasets(); track dataset.id) {
                <option [value]="dataset.id">
                  {{ dataset.name }} · {{ dataset.itemCount }}
                  {{ dataset.itemCount === 1 ? 'item' : 'items' }}
                </option>
              }
            </select>
          </label>

          @if (runsStore.createError(); as error) {
            <p
              class="rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive">
              {{ error }}
            </p>
          }
        </div>

        <footer class="flex justify-end gap-2 border-t px-7 py-4">
          <button z-button zType="outline" type="button" (click)="dismissed.emit()">Cancel</button>
          <button
            z-button
            type="button"
            [zLoading]="runsStore.isCreating()"
            [zDisabled]="submitDisabled()"
            (click)="submit()">
            <ng-icon name="lucidePlay" aria-hidden="true" />
            Start run
          </button>
        </footer>
      </section>
    </div>
  `,
})
export class RunCreateModal {
  protected readonly runsStore = inject(RunsStore);
  protected readonly promptsStore = inject(SystemPromptsStore);
  protected readonly datasetsStore = inject(DatasetsStore);

  readonly dismissed = output<void>();

  protected readonly systemPromptId = signal('');
  protected readonly datasetId = signal('');
  protected readonly attemptedSubmit = signal(false);

  protected readonly submitDisabled = computed(
    () => this.runsStore.isCreating() || !this.systemPromptId() || !this.datasetId(),
  );

  submit(): void {
    this.attemptedSubmit.set(true);
    if (this.submitDisabled()) {
      return;
    }

    this.runsStore
      .createRun({
        systemPromptId: this.systemPromptId(),
        datasetId: this.datasetId(),
      })
      .subscribe({
        next: () => this.dismissed.emit(),
        error: (err) => console.error(err),
      });
  }
}
