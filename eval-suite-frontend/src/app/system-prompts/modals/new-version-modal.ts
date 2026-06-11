import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideX } from '@ng-icons/lucide';

import { ZardButtonComponent } from '@/shared/button';
import { ZardInputDirective } from '@/shared/input';

import { type SystemPrompt, type SystemPromptRequest } from '../system-prompts-api';
import { SystemPromptsStore } from '../system-prompts-store';

@Component({
  selector: 'evl-system-prompt-new-version-modal',
  imports: [FormField, NgIcon, ZardButtonComponent, ZardInputDirective],
  providers: [provideIcons({ lucideX })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="systemPromptNewVersionModalTitle"
        class="flex max-h-[88vh] w-full max-w-2xl flex-col overflow-hidden rounded-xl border bg-card shadow-lg">
        <header class="flex items-start justify-between gap-4 border-b px-7 py-5">
          <div class="min-w-0 space-y-2">
            <div class="flex items-center gap-2">
              <h2
                id="systemPromptNewVersionModalTitle"
                class="truncate text-xl font-semibold text-foreground"
                [title]="sourcePrompt().name">
                {{ sourcePrompt().name }}
              </h2>

              <span
                class="rounded-md border bg-muted px-2 py-0.5 text-xs font-medium text-muted-foreground">
                v{{ nextVersionNumber() }}
              </span>
            </div>

            <p class="text-sm text-muted-foreground">Create a new version of this prompt.</p>
          </div>

          <button z-button zType="outline" type="button" (click)="dismiss()">
            <ng-icon name="lucideX" aria-hidden="true" />
            Close
          </button>
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
                <div class="flex items-center justify-between gap-3">
                  <label
                    for="newVersionPromptContentInput"
                    class="text-sm font-medium text-foreground">
                    System Prompt Content
                  </label>

                  <span
                    class="text-xs text-muted-foreground"
                    [class.text-destructive]="contentLength() > 5000">
                    {{ contentLength() }} / 5000
                  </span>
                </div>

                <textarea
                  z-input
                  id="newVersionPromptContentInput"
                  rows="9"
                  [formField]="promptForm.content"
                  [zStatus]="
                    validationStatus(
                      (promptForm.content().touched() &&
                        promptForm.content().errors().length > 0) ||
                        contentLength() > 5000
                    )
                  "
                  placeholder="Write the LLM system prompt instructions here..."
                  class="min-h-[48vh] resize-y font-mono text-sm leading-6"
                  [attr.aria-invalid]="
                    promptForm.content().errors().length ? 'true' : null
                  "></textarea>

                @if (promptForm.content().touched() && promptForm.content().errors().length) {
                  <p class="text-xs text-destructive">
                    {{ promptForm.content().errors()[0].message }}
                  </p>
                }
              </div>
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
              Save Version
            </button>
          </footer>
        </form>
      </section>
    </div>
  `,
})
export class SystemPromptNewVersionModal implements OnInit {
  readonly sourcePrompt = input.required<SystemPrompt>();
  readonly dismissed = output<void>();

  protected readonly store = inject(SystemPromptsStore);

  protected readonly promptModel = signal({
    content: '',
    familyId: '',
  });

  protected readonly promptForm = form(this.promptModel, (s) => {
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

  protected readonly nextVersionNumber = computed(() => this.sourcePrompt().versionNumber + 1);

  protected readonly displayErrorMessage = computed(() => {
    return this.store.createError() ?? '';
  });

  protected validationStatus(hasError: boolean): 'error' | undefined {
    return hasError ? 'error' : undefined;
  }

  ngOnInit(): void {
    this.store.clearCreateError();

    const prompt = this.sourcePrompt();

    this.promptModel.set({
      content: prompt.content,
      familyId: prompt.familyId,
    });
    this.promptForm().reset();
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
        name: this.sourcePrompt().name,
        content: modelValue.content,
        familyId: modelValue.familyId,
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
