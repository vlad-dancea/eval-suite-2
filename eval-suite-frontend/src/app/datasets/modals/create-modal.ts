import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { applyEach, FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucidePlus, lucideTrash2, lucideX } from '@ng-icons/lucide';

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardButtonComponent } from '@/shared/button';
import { ZardDividerComponent } from '@/shared/divider';
import { ZardInputDirective } from '@/shared/input';

import { type DatasetRequest } from '../datasets-api';
import { DatasetsStore } from '../datasets-store';

interface DatasetItemModel {
  id: string;
  input: string;
  expectedOutput: string;
}

@Component({
  selector: 'evl-dataset-create-modal',
  imports: [
    FormField,
    NgIcon,
    ZardBadgeComponent,
    ZardButtonComponent,
    ZardDividerComponent,
    ZardInputDirective,
  ],
  providers: [provideIcons({ lucidePlus, lucideTrash2, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="datasetCreateModalTitle"
        class="flex h-[88vh] w-full max-w-6xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="space-y-1">
            <h2 id="datasetCreateModalTitle" class="text-lg font-semibold text-foreground">
              Create Dataset
            </h2>

            <p class="text-sm text-muted-foreground">
              Add reusable input and expected-output examples for future runs.
            </p>
          </div>
        </header>

        <form
          novalidate
          (submit)="onSubmit(); $event.preventDefault()"
          class="flex min-h-0 flex-1 flex-col">
          <div
            class="grid min-h-0 flex-1 grid-cols-1 lg:grid-cols-[minmax(16rem,20rem)_auto_minmax(0,1fr)]">
            <aside class="space-y-6 p-7">
              @if (displayErrorMessage()) {
                <div
                  role="alert"
                  class="rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
                  {{ displayErrorMessage() }}
                </div>
              }

              <div class="space-y-2">
                <label for="datasetNameInput" class="text-sm font-medium text-foreground">
                  Dataset name
                </label>

                <input
                  z-input
                  id="datasetNameInput"
                  type="text"
                  [formField]="datasetForm.name"
                  [zStatus]="
                    validationStatus(
                      datasetForm.name().touched() && datasetForm.name().errors().length > 0
                    )
                  "
                  placeholder="e.g. Customer support questions"
                  [attr.aria-invalid]="datasetForm.name().errors().length ? 'true' : null"
                  aria-describedby="datasetNameHelp" />

                @if (datasetForm.name().touched() && datasetForm.name().errors().length) {
                  <p class="text-xs text-destructive">
                    {{ datasetForm.name().errors()[0].message }}
                  </p>
                }

                <p id="datasetNameHelp" class="text-xs leading-5 text-muted-foreground">
                  Use a short name that makes this dataset recognizable when starting a run.
                </p>
              </div>

              <div class="space-y-4 border-t pt-6">
                <div class="flex items-center justify-between gap-3">
                  <p class="text-sm font-medium text-foreground">Dataset items</p>

                  <z-badge zType="secondary" class="h-5 px-1.5 text-xs font-medium">
                    {{ model().items.length }}
                    {{ model().items.length === 1 ? 'item' : 'items' }}
                  </z-badge>
                </div>

                <p class="mt-2 text-xs leading-5 text-muted-foreground">
                  Each item pairs the model input with the reference answer it should be judged
                  against.
                </p>

                <button
                  z-button
                  zType="outline"
                  zSize="sm"
                  type="button"
                  (click)="addItem()"
                  class="w-full">
                  <ng-icon name="lucidePlus" aria-hidden="true" />
                  Add item
                </button>
              </div>
            </aside>

            <z-divider zOrientation="horizontal" zSpacing="none" class="lg:hidden" />
            <z-divider zOrientation="vertical" zSpacing="none" class="hidden lg:block" />

            <div class="min-h-0 overflow-y-auto px-7 py-8">
              <ol class="space-y-8">
                @for (row of model().items; track row.id; let i = $index) {
                  <li class="border-b pb-4 last:border-b-0 last:pb-0">
                    <div class="grid gap-6 xl:grid-cols-2">
                      <div class="space-y-2">
                        <div class="flex items-center justify-between gap-3">
                          <label
                            [attr.for]="'datasetItemInput' + i"
                            class="text-sm font-medium text-foreground">
                            Input
                          </label>

                          <span
                            class="text-xs text-muted-foreground"
                            [class.text-destructive]="row.input.length > 5000">
                            {{ row.input.length }} / 5000
                          </span>
                        </div>

                        <textarea
                          z-input
                          [id]="'datasetItemInput' + i"
                          rows="5"
                          [formField]="datasetForm.items[i].input"
                          [zStatus]="
                            validationStatus(
                              (datasetForm.items[i].input().touched() &&
                                datasetForm.items[i].input().errors().length > 0) ||
                                row.input.length > 5000
                            )
                          "
                          placeholder="The input sent to the model..."
                          class="min-h-40 resize-y font-mono text-sm leading-6"
                          [attr.aria-invalid]="
                            datasetForm.items[i].input().errors().length ? 'true' : null
                          "></textarea>
                        @if (
                          datasetForm.items[i].input().touched() &&
                          datasetForm.items[i].input().errors().length
                        ) {
                          <p class="text-xs text-destructive">
                            {{ datasetForm.items[i].input().errors()[0].message }}
                          </p>
                        }
                      </div>

                      <div class="space-y-2">
                        <div class="flex items-center justify-between gap-3">
                          <label
                            [attr.for]="'datasetItemExpected' + i"
                            class="text-sm font-medium text-foreground">
                            Expected output
                          </label>

                          <span
                            class="text-xs text-muted-foreground"
                            [class.text-destructive]="row.expectedOutput.length > 5000">
                            {{ row.expectedOutput.length }} / 5000
                          </span>
                        </div>

                        <textarea
                          z-input
                          [id]="'datasetItemExpected' + i"
                          rows="5"
                          [formField]="datasetForm.items[i].expectedOutput"
                          [zStatus]="
                            validationStatus(
                              (datasetForm.items[i].expectedOutput().touched() &&
                                datasetForm.items[i].expectedOutput().errors().length > 0) ||
                                row.expectedOutput.length > 5000
                            )
                          "
                          placeholder="The expected reference answer..."
                          class="min-h-40 resize-y font-mono text-sm leading-6"
                          [attr.aria-invalid]="
                            datasetForm.items[i].expectedOutput().errors().length ? 'true' : null
                          "></textarea>

                        @if (
                          datasetForm.items[i].expectedOutput().touched() &&
                          datasetForm.items[i].expectedOutput().errors().length
                        ) {
                          <p class="text-xs text-destructive">
                            {{ datasetForm.items[i].expectedOutput().errors()[0].message }}
                          </p>
                        }
                      </div>
                    </div>
                    <div class="w-full mt-2 flex items-center justify-end">
                      @if (model().items.length > 1) {
                        <button
                          z-button
                          zType="outline"
                          zSize="sm"
                          type="button"
                          (click)="removeItem(i)"
                          class="text-xs text-destructive">
                          <ng-icon name="lucideTrash2" />
                          Remove
                        </button>
                      }
                    </div>
                  </li>
                }
              </ol>
            </div>
          </div>

          <footer class="flex items-center justify-end gap-2 border-t px-7 py-4">
            <button z-button zType="outline" type="button" (click)="dismiss()">Cancel</button>

            <button
              z-button
              [zLoading]="store.isCreating()"
              [zDisabled]="isSubmitDisabled()"
              type="submit"
              [disabled]="isSubmitDisabled()"
              [class.cursor-wait]="isSubmitDisabled()">
              Save Dataset
            </button>
          </footer>
        </form>
      </section>
    </div>
  `,
})
export class DatasetCreateModal implements OnInit {
  protected readonly store = inject(DatasetsStore);

  readonly dismissed = output<void>();

  private itemSeq = 0;

  protected readonly model = signal<{ name: string; items: DatasetItemModel[] }>({
    name: '',
    items: [this.createItem()],
  });

  protected readonly datasetForm = form(this.model, (path) => {
    required(path.name, { message: 'Name is required' });
    maxLength(path.name, 120, { message: 'Name must not exceed 120 characters' });

    applyEach(path.items, (item) => {
      required(item.input, { message: 'Input is required' });
      maxLength(item.input, 5000, { message: 'Input must not exceed 5000 characters' });
      required(item.expectedOutput, { message: 'Expected output is required' });
      maxLength(item.expectedOutput, 5000, {
        message: 'Expected output must not exceed 5000 characters',
      });
    });
  });

  protected readonly isSubmitDisabled = computed(() => {
    return this.datasetForm().invalid() || this.datasetForm().pending() || this.store.isCreating();
  });

  protected readonly displayErrorMessage = computed(() => {
    return this.store.createError() ?? '';
  });

  protected validationStatus(hasError: boolean): 'error' | undefined {
    return hasError ? 'error' : undefined;
  }

  ngOnInit(): void {
    this.store.clearCreateError();
  }

  protected addItem(): void {
    this.model.update((current) => ({
      ...current,
      items: [...current.items, this.createItem()],
    }));
  }

  protected removeItem(index: number): void {
    this.model.update((current) => {
      if (current.items.length === 1) {
        return current;
      }
      return {
        ...current,
        items: current.items.filter((_, i) => i !== index),
      };
    });
  }

  protected dismiss(): void {
    this.store.clearCreateError();
    this.dismissed.emit();
  }

  protected onSubmit(): void {
    this.store.clearCreateError();

    submit(this.datasetForm, async () => {
      const value = this.model();
      const request: DatasetRequest = {
        name: value.name,
        items: value.items.map((item) => ({
          input: item.input,
          expectedOutput: item.expectedOutput,
        })),
      };

      this.store.createDataset(request).subscribe({
        next: () => {
          this.dismissed.emit();
        },
        error: () => undefined,
      });
    });
  }

  private createItem(): DatasetItemModel {
    this.itemSeq += 1;
    return { id: `item-${this.itemSeq}`, input: '', expectedOutput: '' };
  }
}
