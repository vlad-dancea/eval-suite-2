import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideArchive, lucideSquarePen, lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';

import { type SystemPrompt } from '../system-prompts-api';
import { SystemPromptsStore } from '../system-prompts-store';
import { ArchiveConfirmationModal } from './archive-modal';
import { SystemPromptContentModal } from './content-modal';

@Component({
  selector: 'evl-system-prompt-version-modal',
  imports: [
    ArchiveConfirmationModal,
    DatePipe,
    NgIcon,
    SystemPromptContentModal,
    ZardButtonComponent,
  ],
  providers: [provideIcons({ lucideArchive, lucideSquarePen, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="promptVersionHistoryModalTitle"
        class="flex max-h-[88vh] w-full max-w-4xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="space-y-1">
            <h2 id="promptVersionHistoryModalTitle" class="text-lg font-semibold text-foreground">
              Version History
            </h2>

            <p class="text-sm text-muted-foreground">
              {{ familyName() }}
            </p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" class="h-4 w-4" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="flex-1 overflow-y-auto px-7 py-8">
          @if (store.isLoadingHistory()) {
            <div class="flex min-h-72 flex-col items-center justify-center">
              <div
                class="h-8 w-8 animate-spin rounded-full border-2 border-muted border-t-foreground"></div>
              <p class="mt-4 text-sm text-muted-foreground">Fetching history...</p>
            </div>
          } @else {
            <div class="space-y-8">
              @for (version of store.history(); track version.id) {
                @let isArchived = version.deletedAt !== null;

                <article class="border-b pb-8 last:border-b-0 last:pb-0">
                  <div
                    class="mb-4 flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                    <div class="flex items-center gap-2">
                      <h3 class="text-sm font-medium text-foreground">
                        v{{ version.versionNumber }}
                      </h3>

                      @if (isArchived) {
                        <span class="text-xs text-muted-foreground">Archived</span>
                      }
                    </div>

                    <time class="text-xs text-muted-foreground">
                      {{ version.createdAt | date: 'medium' }}
                    </time>
                  </div>

                  <div class="rounded-lg border border-input bg-transparent p-4 dark:bg-input/30">
                    <p class="break-words font-mono text-xs leading-6 text-foreground">
                      {{ version.preview }}
                      @if (version.content.length > 100) {
                        <span>...</span>
                      }
                    </p>

                    @if (version.content.length > 100) {
                      <div class="mt-3 flex justify-end">
                        <button
                          z-button
                          zType="link"
                          type="button"
                          (click)="openContentModal(version)"
                          class="h-auto gap-1 p-0 text-xs text-muted-foreground hover:text-foreground">
                          Show more
                        </button>
                      </div>
                    }
                  </div>
                </article>
              }
            </div>
          }
        </div>

        @if (currentPrompt(); as prompt) {
          <footer class="flex items-center justify-end gap-2 border-t px-7 py-4">
            <button
              z-button
              zType="outline"
              type="button"
              (click)="newVersionRequested.emit(prompt)">
              <ng-icon name="lucideSquarePen" class="h-4 w-4" aria-hidden="true" />
              New Version
            </button>

            <button
              z-button
              zType="outline"
              type="button"
              (click)="openArchiveModal(prompt)"
              class="text-destructive hover:text-destructive">
              <ng-icon name="lucideArchive" class="h-4 w-4" aria-hidden="true" />
              Archive Prompt
            </button>
          </footer>
        }
      </section>

      @if (selectedPrompt(); as prompt) {
        <evl-system-prompt-archive-modal
          [prompt]="prompt"
          (confirmed)="confirmArchivePrompt()"
          (dismissed)="closeArchiveModal()" />
      }

      @if (selectedContentPrompt(); as prompt) {
        <evl-system-prompt-content-modal [prompt]="prompt" (dismissed)="closeContentModal()" />
      }
    </div>
  `,
})
export class SystemPromptVersionModal {
  readonly familyName = input.required<string>();
  readonly dismissed = output<void>();
  readonly newVersionRequested = output<SystemPrompt>();

  protected readonly store = inject(SystemPromptsStore);
  protected readonly selectedPrompt = signal<SystemPrompt | null>(null);
  protected readonly selectedContentPrompt = signal<SystemPrompt | null>(null);

  protected readonly currentPrompt = computed(() => {
    return this.store.history().find((version) => version.deletedAt === null) ?? null;
  });

  protected openContentModal(prompt: SystemPrompt): void {
    this.selectedContentPrompt.set(prompt);
  }

  protected closeContentModal(): void {
    this.selectedContentPrompt.set(null);
  }

  protected openArchiveModal(prompt: SystemPrompt): void {
    this.selectedPrompt.set(prompt);
  }

  protected closeArchiveModal(): void {
    this.selectedPrompt.set(null);
  }

  protected confirmArchivePrompt(): void {
    const prompt = this.selectedPrompt();
    if (!prompt) {
      return;
    }

    this.store.archivePrompt(prompt.id).subscribe({
      next: () => {
        this.closeArchiveModal();
      },
      error: (err) => {
        console.error(err);
        alert('Failed to archive prompt.');
      },
    });
  }
}
