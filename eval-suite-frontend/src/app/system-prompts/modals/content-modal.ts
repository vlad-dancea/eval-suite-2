import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideX } from '@ng-icons/lucide';

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardButtonComponent } from '@/shared/button';

import { type SystemPrompt } from '../system-prompts-api';

@Component({
  selector: 'evl-system-prompt-content-modal',
  imports: [DatePipe, NgIcon, ZardBadgeComponent, ZardButtonComponent],
  providers: [provideIcons({ lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="promptContentModalTitle"
        class="flex max-h-[85vh] w-full max-w-3xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-6 py-4">
          <div class="min-w-0 space-y-2">
            <div class="flex items-center gap-2">
              <z-badge zType="secondary" class="h-5 px-1.5 text-xs font-medium">
                v{{ prompt().versionNumber }}
              </z-badge>

              <time class="text-xs text-muted-foreground">
                {{ prompt().createdAt | date: 'medium' }}
              </time>
            </div>

            <h2
              id="promptContentModalTitle"
              class="truncate text-lg font-semibold text-foreground"
              [title]="prompt().name">
              {{ prompt().name }}
            </h2>
          </div>

          <button z-button zType="outline" type="button" (click)="dismissed.emit()">
            <ng-icon name="lucideX" class="h-4 w-4" aria-hidden="true" />
            Close
          </button>
        </header>

        <div class="flex-1 overflow-y-auto p-6">
          <p
            class="whitespace-pre-wrap break-words font-mono text-sm leading-6 text-muted-foreground">
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
