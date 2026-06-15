import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { DatasetsStore } from '@/datasets/datasets-store';
import { ZardButtonComponent } from '@/shared/button';
import { ZardInputDirective } from '@/shared/input';
import { SystemPromptsStore } from '@/system-prompts/system-prompts-store';

import { type CreateRunRequest } from '../runs.api';
import { RunsStore } from '../runs.store';

@Component({
  selector: 'evl-create-run-modal',
  imports: [ZardButtonComponent, ZardInputDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="createRunModalTitle"
        class="flex max-h-[88vh] w-full max-w-2xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="border-b px-7 py-5">
          <div class="space-y-1">
            <h2 id="createRunModalTitle" class="text-lg font-semibold text-foreground">
              Create Run
            </h2>
          </div>
        </header>

        <form
          novalidate
          (submit)="onSubmit(); $event.preventDefault()"
          class="flex min-h-0 flex-1 flex-col">
          <div class="flex-1 space-y-6 overflow-y-auto p-7">
            @if (displayErrorMessage()) {
              <div
                role="alert"
                class="rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
                {{ displayErrorMessage() }}
              </div>
            }

            <div class="space-y-5">
              <div class="space-y-2">
                <label for="runPromptInput" class="text-sm font-medium text-foreground">
                  System Prompt
                </label>

                <div class="relative">
                  <input
                    z-input
                    id="runPromptInput"
                    type="text"
                    readonly
                    role="combobox"
                    aria-controls="runPromptOptions"
                    [attr.aria-expanded]="isPromptListOpen()"
                    [value]="selectedSystemPromptLabel()"
                    [placeholder]="
                      systemPromptsStore.isLoadingActivePrompts()
                        ? 'Loading prompts...'
                        : 'Select a prompt'
                    "
                    [disabled]="systemPromptsStore.isLoadingActivePrompts()"
                    [zStatus]="validationStatus(systemPromptError() !== '')"
                    class="cursor-pointer"
                    (click)="openPromptList()"
                    (focus)="openPromptList()" />

                  @if (isPromptListOpen()) {
                    <div
                      id="runPromptOptions"
                      role="listbox"
                      class="absolute z-10 mt-2 max-h-64 w-full overflow-y-auto rounded-md border bg-popover p-1 shadow-md">
                      @for (prompt of systemPromptsStore.activePrompts(); track prompt.id) {
                        <button
                          type="button"
                          role="option"
                          class="flex w-full items-center justify-between gap-3 rounded-sm px-3 py-2 text-left text-sm text-popover-foreground outline-none hover:bg-accent hover:text-accent-foreground focus-visible:bg-accent focus-visible:text-accent-foreground"
                          [attr.aria-selected]="selectedSystemPromptId() === prompt.id"
                          (click)="selectSystemPrompt(prompt.id)">
                          <span class="truncate">{{ prompt.name }}</span>
                          <span class="shrink-0 text-xs text-muted-foreground">
                            v{{ prompt.versionNumber }}
                          </span>
                        </button>
                      }
                    </div>
                  }
                </div>

                <input
                  type="hidden"
                  [value]="selectedSystemPromptId()"
                  [attr.aria-invalid]="systemPromptError() ? 'true' : null"
                  aria-hidden="true" />

                @if (systemPromptsStore.isLoadingActivePrompts()) {
                  <p class="text-xs text-muted-foreground">Loading prompts...</p>
                } @else if (systemPromptsStore.activePromptsError()) {
                  <p class="text-xs text-destructive">Could not load system prompts.</p>
                } @else if (systemPromptsStore.activePrompts().length === 0) {
                  <p class="text-xs text-muted-foreground">
                    Create a system prompt before starting a run.
                  </p>
                } @else if (systemPromptError()) {
                  <p class="text-xs text-destructive">{{ systemPromptError() }}</p>
                }
              </div>

              <div class="space-y-2">
                <label for="runDatasetInput" class="text-sm font-medium text-foreground">
                  Dataset
                </label>

                <div class="relative">
                  <input
                    z-input
                    id="runDatasetInput"
                    type="text"
                    readonly
                    role="combobox"
                    aria-controls="runDatasetOptions"
                    [attr.aria-expanded]="isDatasetListOpen()"
                    [value]="selectedDatasetLabel()"
                    [placeholder]="
                      datasetsStore.isLoadingActiveDatasets()
                        ? 'Loading datasets...'
                        : 'Select a dataset'
                    "
                    [disabled]="datasetsStore.isLoadingActiveDatasets()"
                    [zStatus]="validationStatus(datasetError() !== '')"
                    class="cursor-pointer"
                    (click)="openDatasetList()"
                    (focus)="openDatasetList()" />

                  @if (isDatasetListOpen()) {
                    <div
                      id="runDatasetOptions"
                      role="listbox"
                      class="absolute z-10 mt-2 max-h-64 w-full overflow-y-auto rounded-md border bg-popover p-1 shadow-md">
                      @for (dataset of datasetsStore.activeDatasets(); track dataset.id) {
                        <button
                          type="button"
                          role="option"
                          class="flex w-full items-center justify-between gap-3 rounded-sm px-3 py-2 text-left text-sm text-popover-foreground outline-none hover:bg-accent hover:text-accent-foreground focus-visible:bg-accent focus-visible:text-accent-foreground"
                          [attr.aria-selected]="selectedDatasetId() === dataset.id"
                          (click)="selectDataset(dataset.id)">
                          <span class="truncate">{{ dataset.name }}</span>
                          <span class="shrink-0 text-xs text-muted-foreground">
                            {{ dataset.itemCount }}
                            {{ dataset.itemCount === 1 ? 'item' : 'items' }}
                          </span>
                        </button>
                      }
                    </div>
                  }
                </div>

                <input
                  type="hidden"
                  [value]="selectedDatasetId()"
                  [attr.aria-invalid]="datasetError() ? 'true' : null"
                  aria-hidden="true" />

                @if (datasetsStore.isLoadingActiveDatasets()) {
                  <p class="text-xs text-muted-foreground">Loading datasets...</p>
                } @else if (datasetsStore.activeDatasetsError()) {
                  <p class="text-xs text-destructive">Could not load datasets.</p>
                } @else if (datasetsStore.activeDatasets().length === 0) {
                  <p class="text-xs text-muted-foreground">
                    Create a dataset before starting a run.
                  </p>
                } @else if (datasetError()) {
                  <p class="text-xs text-destructive">{{ datasetError() }}</p>
                }
              </div>

              <label for="automaticImprovementInput" class="flex items-start gap-3">
                <input
                  id="automaticImprovementInput"
                  type="checkbox"
                  class="mt-0.5 h-4 w-4 rounded border-input accent-primary"
                  [checked]="automaticImprovementEnabled()"
                  (change)="toggleAutomaticImprovement($event)" />

                <span class="space-y-1">
                  <span class="block text-sm font-medium text-foreground">
                    Automatic prompt improvement
                  </span>
                  <span class="block text-sm leading-5 text-muted-foreground">
                    The judge will continuously run and improve the initial prompt until it
                    stablizes.
                  </span>
                </span>
              </label>
            </div>
          </div>

          <footer class="flex items-center justify-end gap-2 border-t px-7 py-4">
            <button z-button zType="outline" type="button" (click)="dismiss()">Cancel</button>

            <button
              z-button
              [zLoading]="runsStore.isCreating()"
              [zDisabled]="isSubmitDisabled()"
              type="submit"
              [disabled]="isSubmitDisabled()"
              [class.cursor-wait]="isSubmitDisabled()">
              Create Run
            </button>
          </footer>
        </form>
      </section>
    </div>
  `,
})
export class CreateRunModal implements OnInit {
  protected readonly runsStore = inject(RunsStore);
  protected readonly systemPromptsStore = inject(SystemPromptsStore);
  protected readonly datasetsStore = inject(DatasetsStore);

  readonly dismissed = output<void>();

  protected readonly selectedSystemPromptId = signal('');
  protected readonly selectedDatasetId = signal('');
  protected readonly automaticImprovementEnabled = signal(false);
  protected readonly hasSubmitted = signal(false);
  protected readonly isPromptListOpen = signal(false);
  protected readonly isDatasetListOpen = signal(false);

  protected readonly selectedSystemPromptLabel = computed(() => {
    const selectedId = this.selectedSystemPromptId();
    const prompt = this.systemPromptsStore
      .activePrompts()
      .find((candidate) => candidate.id === selectedId);

    return prompt ? `${prompt.name} · v${prompt.versionNumber}` : '';
  });

  protected readonly selectedDatasetLabel = computed(() => {
    const selectedId = this.selectedDatasetId();
    const dataset = this.datasetsStore
      .activeDatasets()
      .find((candidate) => candidate.id === selectedId);

    if (!dataset) {
      return '';
    }

    const itemLabel = dataset.itemCount === 1 ? 'item' : 'items';
    return `${dataset.name} · ${dataset.itemCount} ${itemLabel}`;
  });

  protected readonly systemPromptError = computed(() => {
    if (!this.hasSubmitted() || this.selectedSystemPromptId()) {
      return '';
    }

    return 'Select a system prompt.';
  });

  protected readonly datasetError = computed(() => {
    if (!this.hasSubmitted() || this.selectedDatasetId()) {
      return '';
    }

    return 'Select a dataset.';
  });

  protected readonly displayErrorMessage = computed(() => {
    return this.runsStore.createError() ?? '';
  });

  protected readonly isSubmitDisabled = computed(() => {
    return (
      this.runsStore.isCreating() ||
      this.systemPromptsStore.isLoadingActivePrompts() ||
      this.datasetsStore.isLoadingActiveDatasets() ||
      Boolean(this.systemPromptsStore.activePromptsError()) ||
      Boolean(this.datasetsStore.activeDatasetsError()) ||
      this.systemPromptsStore.activePrompts().length === 0 ||
      this.datasetsStore.activeDatasets().length === 0
    );
  });

  ngOnInit(): void {
    this.runsStore.clearCreateError();
    this.systemPromptsStore.reloadActivePrompts();
    this.datasetsStore.reloadActiveDatasets();
  }

  protected validationStatus(hasError: boolean): 'error' | undefined {
    return hasError ? 'error' : undefined;
  }

  protected openPromptList(): void {
    if (this.systemPromptsStore.activePrompts().length > 0) {
      this.isPromptListOpen.set(true);
      this.isDatasetListOpen.set(false);
    }
  }

  protected selectSystemPrompt(promptId: string): void {
    this.selectedSystemPromptId.set(promptId);
    this.isPromptListOpen.set(false);
  }

  protected openDatasetList(): void {
    if (this.datasetsStore.activeDatasets().length > 0) {
      this.isDatasetListOpen.set(true);
      this.isPromptListOpen.set(false);
    }
  }

  protected selectDataset(datasetId: string): void {
    this.selectedDatasetId.set(datasetId);
    this.isDatasetListOpen.set(false);
  }

  protected toggleAutomaticImprovement(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    this.automaticImprovementEnabled.set(input?.checked ?? false);
  }

  protected dismiss(): void {
    this.runsStore.clearCreateError();
    this.dismissed.emit();
  }

  protected onSubmit(): void {
    this.hasSubmitted.set(true);
    this.runsStore.clearCreateError();

    if (!this.selectedSystemPromptId() || !this.selectedDatasetId() || this.isSubmitDisabled()) {
      return;
    }

    const request: CreateRunRequest = {
      systemPromptId: this.selectedSystemPromptId(),
      datasetId: this.selectedDatasetId(),
      automaticImprovementEnabled: this.automaticImprovementEnabled(),
    };

    this.runsStore.createRun(request).subscribe({
      next: () => {
        this.dismissed.emit();
      },
      error: () => undefined,
    });
  }
}
