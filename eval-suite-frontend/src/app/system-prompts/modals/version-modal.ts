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

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardButtonComponent } from '@/shared/button';
import { ZardCardComponent } from '@/shared/card';

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
    ZardBadgeComponent,
    ZardButtonComponent,
    ZardCardComponent,
  ],
  providers: [provideIcons({ lucideArchive, lucideSquarePen, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        class="flex max-h-[85vh] w-full max-w-3xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-6 py-4">
          <div class="space-y-1">
            <h2 class="text-lg font-semibold text-foreground">Version History</h2>

            <p class="text-sm text-muted-foreground">
              {{ familyName() }}
            </p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" class="h-4 w-4" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="flex-1 overflow-y-auto p-6">
          @if (store.isLoadingHistory()) {
            <div class="flex min-h-72 flex-col items-center justify-center">
              <div
                class="h-8 w-8 animate-spin rounded-full border-2 border-muted border-t-foreground"></div>
              <p class="mt-4 text-sm text-muted-foreground">Fetching history...</p>
            </div>
          } @else {
            <div
              class="relative space-y-5 before:absolute before:bottom-4 before:left-4 before:top-4 before:w-px before:bg-border">
              @for (version of store.history(); track version.id) {
                @let isArchived = version.deletedAt !== null;

                <article class="relative pl-10">
                  <div
                    class="absolute left-[9px] top-4 h-3.5 w-3.5 rounded-full border-2 border-background"
                    [class.bg-foreground]="!isArchived"
                    [class.bg-muted-foreground]="isArchived"></div>

                  <z-card class="bg-background py-5" [class.opacity-60]="isArchived">
                    <div
                      class="mb-4 flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                      <div class="flex items-center gap-2">
                        <z-badge zType="secondary" class="h-5 px-1.5 text-xs font-medium">
                          v{{ version.versionNumber }}
                        </z-badge>

                        @if (isArchived) {
                          <z-badge
                            zType="outline"
                            class="h-5 px-1.5 text-[10px] font-medium text-muted-foreground">
                            Archived
                          </z-badge>
                        }
                      </div>

                      <time class="text-xs text-muted-foreground">
                        {{ version.createdAt | date: 'medium' }}
                      </time>
                    </div>

                    <div
                      class="max-h-52 overflow-y-auto font-mono text-xs leading-5 text-muted-foreground">
                      <p class="break-words">
                        {{ version.preview }}
                        @if (version.content.length > 100) {
                          <span>...</span>
                        }
                      </p>
                    </div>

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
                  </z-card>
                </article>
              }
            </div>
          }
        </div>

        @if (currentPrompt(); as prompt) {
          <footer class="flex items-center justify-end gap-2 border-t px-6 py-4">
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
