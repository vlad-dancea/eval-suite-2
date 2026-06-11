import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideArchive, lucideTriangleAlert, lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';

import { type SystemPrompt } from '../system-prompts-api';

@Component({
  selector: 'evl-system-prompt-archive-modal',
  imports: [NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucideArchive, lucideTriangleAlert, lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="archivePromptModalTitle"
        aria-describedby="archivePromptModalDescription"
        class="flex w-full max-w-md flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 px-5 pb-3 pt-5">
          <div class="flex min-w-0 items-start gap-3">
            <div
              class="mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-destructive/10 text-destructive">
              <ng-icon name="lucideTriangleAlert" class="h-5 w-5" aria-hidden="true" />
            </div>

            <div class="flex flex-col items-start">
              <h2 id="archivePromptModalTitle" class="text-lg font-semibold text-foreground">
                Archive prompt history?
              </h2>

              <p class="truncate text-sm text-muted-foreground" [title]="prompt().name">
                {{ prompt().name }}
              </p>
            </div>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" class="h-4 w-4" aria-hidden="true" />
            Close
          </button>
        </header>

        <div id="archivePromptModalDescription" class="space-y-3 px-5 py-2 text-sm leading-5">
          <p class="text-card-foreground">
            The entire history of this prompt will be deleted and will no longer be available for
            use in new runs.
          </p>

          <p class="text-muted-foreground text-xs">
            When looking at old runs, the prompt text will still be visible.
          </p>

          <p class="font-medium text-card-foreground">Do you want to continue?</p>
        </div>

        <footer class="flex justify-end gap-2 px-5 pb-5 pt-4">
          <button z-button zType="outline" type="button" (click)="dismissed.emit()">Cancel</button>

          <button z-button zType="destructive" type="button" (click)="confirmed.emit()">
            <ng-icon name="lucideArchive" class="h-4 w-4" aria-hidden="true" />
            Continue
          </button>
        </footer>
      </section>
    </div>
  `,
})
export class ArchiveConfirmationModal {
  readonly prompt = input.required<SystemPrompt>();
  readonly confirmed = output<void>();
  readonly dismissed = output<void>();
}
