import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';

import { type SystemPrompt } from '../system-prompts-api';

@Component({
  selector: 'evl-system-prompt-content-modal',
  imports: [DatePipe, NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="promptContentModalTitle"
        class="flex max-h-[88vh] w-full max-w-4xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="min-w-0 space-y-1">
            <h2
              id="promptContentModalTitle"
              class="truncate text-lg font-semibold text-foreground"
              [title]="prompt().name">
              {{ prompt().name }}
            </h2>

            <div class="flex items-center gap-2 text-xs text-muted-foreground">
              <span>v{{ prompt().versionNumber }}</span>
              <span aria-hidden="true">/</span>
              <time>
                {{ prompt().createdAt | date: 'medium' }}
              </time>
            </div>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="flex-1 overflow-y-auto px-7 py-8">
          <p
            class="mx-auto max-w-3xl whitespace-pre-wrap break-words rounded-lg border border-input bg-transparent p-5 font-mono text-xs leading-6 text-foreground dark:bg-input/30">
            {{ prompt().content }}
          </p>
        </div>
      </section>
    </div>
  `,
})
export class SystemPromptContentModal {
  readonly prompt = input.required<SystemPrompt>();
  readonly dismissed = output<void>();
}
