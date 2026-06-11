import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';

import { type SystemPromptRequest } from '../system-prompts-api';
import { SystemPromptsStore } from '../system-prompts-store';

@Component({
  selector: 'evl-system-prompt-create-modal',
  imports: [FormField, NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        class="flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-6 py-4">
          <div class="space-y-1">
            <h2 class="text-lg font-semibold text-foreground">Create System Prompt</h2>

            <p class="text-sm text-muted-foreground">Add a new prompt.</p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismiss()">
            <ng-icon name="lucideX" class="h-4 w-4" aria-hidden="true" />
            Close
          </button>
        </header>

        <form
          (submit)="onSubmit(); $event.preventDefault()"
          class="flex-1 space-y-6 overflow-y-auto p-6">
          @if (displayErrorMessage()) {
            <div
              role="alert"
              class="rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
              {{ displayErrorMessage() }}
            </div>
          }

          <div class="space-y-5">
            <div class="space-y-2">
              <label for="promptNameInput" class="text-sm font-medium text-foreground">
                Name
              </label>

              <input
                id="promptNameInput"
                type="text"
                [formField]="promptForm.name"
                placeholder="e.g. Chatbot Assistant"
                class="flex h-10 w-full rounded-md border bg-background px-3 py-2 text-sm text-foreground shadow-sm outline-none transition-colors placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50" />

              @if (promptForm.name().touched() && promptForm.name().errors().length) {
                <p class="text-xs text-destructive">
                  {{ promptForm.name().errors()[0].message }}
                </p>
              }
            </div>

            <div class="space-y-2">
              <div class="flex items-center justify-between gap-3">
                <label for="promptContentInput" class="text-sm font-medium text-foreground">
                  System Prompt Content
                </label>

                <span
                  class="text-xs text-muted-foreground"
                  [class.text-destructive]="contentLength() > 5000">
                  {{ contentLength() }} / 5000
                </span>
              </div>

              <textarea
                id="promptContentInput"
                rows="9"
                [formField]="promptForm.content"
                placeholder="Write the LLM system prompt instructions here..."
                class="flex min-h-52 w-full resize-y rounded-md border bg-background px-3 py-2 font-mono text-sm leading-6 text-foreground shadow-sm outline-none transition-colors placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"></textarea>

              @if (promptForm.content().touched() && promptForm.content().errors().length) {
                <p class="text-xs text-destructive">
                  {{ promptForm.content().errors()[0].message }}
                </p>
              }
            </div>
          </div>

          <footer class="flex items-center justify-end gap-2 border-t pt-6">
            <button z-button zType="outline" type="button" (click)="dismiss()">Cancel</button>

            <button
              z-button
              [zLoading]="store.isCreating()"
              [zDisabled]="isSubmitDisabled()"
              type="submit"
              [disabled]="isSubmitDisabled()"
              [class.cursor-wait]="isSubmitDisabled()">
              Save Prompt
            </button>
          </footer>
        </form>
      </section>
    </div>
  `,
})
export class SystemPromptCreateModal implements OnInit {
  protected readonly store = inject(SystemPromptsStore);

  readonly dismissed = output<void>();

  protected readonly promptModel = signal({
    name: '',
    content: '',
  });

  protected readonly promptForm = form(this.promptModel, (s) => {
    required(s.name, { message: 'Name is required' });
    maxLength(s.name, 120, { message: 'Name must not exceed 120 characters' });
    required(s.content, { message: 'Content is required' });
    maxLength(s.content, 5000, {
      message: 'Content must not exceed 5000 characters',
    });
  });

  protected readonly isSubmitDisabled = computed(() => {
    return this.promptForm().invalid() || this.promptForm().pending() || this.store.isCreating();
  });

  protected readonly contentLength = computed(() => {
    return this.promptModel().content.length;
  });

  protected readonly displayErrorMessage = computed(() => {
    return this.store.createError() ?? '';
  });

  ngOnInit(): void {
    this.store.clearCreateError();
  }

  protected dismiss(): void {
    this.store.clearCreateError();
    this.dismissed.emit();
  }

  protected onSubmit(): void {
    this.store.clearCreateError();

    submit(this.promptForm, async () => {
      const modelValue = this.promptModel();
      const request: SystemPromptRequest = {
        name: modelValue.name,
        content: modelValue.content,
      };

      this.store.createPrompt(request).subscribe({
        next: () => {
          this.dismissed.emit();
        },
        error: () => undefined,
      });
    });
  }
}
