import { inject, signal, computed, Service } from '@angular/core';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { SystemPromptsApi, SystemPrompt, SystemPromptRequest } from './system-prompts-api';

@Service()
export class SystemPromptsStore {
  private readonly api = inject(SystemPromptsApi);

  // === Active Prompts ===
  private readonly activePromptsResource = httpResource<SystemPrompt[]>(
    () => {
      return {
        url: this.api.activePrompts(),
      };
    },
    { defaultValue: [] },
  );

  readonly activePrompts = computed(() => this.activePromptsResource.value());
  readonly isLoadingActivePrompts = computed(() => this.activePromptsResource.isLoading());
  readonly activePromptsError = computed(() => this.activePromptsResource.error());

  reloadActivePrompts() {
    this.activePromptsResource.reload();
  }

  // === HISTORY ===
  readonly selectedFamilyId = signal<string | null>(null);

  private readonly historyResource = httpResource<SystemPrompt[]>(
    () => {
      const familyId = this.selectedFamilyId();
      if (!familyId) {
        return undefined;
      }

      return {
        url: this.api.familyHistory(familyId),
      };
    },
    { defaultValue: [] },
  );

  readonly history = computed(() => this.historyResource.value());
  readonly isLoadingHistory = computed(() => this.historyResource.isLoading());
  readonly historyError = computed(() => this.historyResource.error());

  reloadHistory() {
    this.historyResource.reload();
  }

  selectFamily(familyId: string | null) {
    this.selectedFamilyId.set(familyId);
  }

  // === CREATE / ARCHIVE ===
  readonly isCreating = signal<boolean>(false);
  readonly createError = signal<string | null>(null);
  readonly isArchiving = signal<boolean>(false);
  readonly deleteError = signal<string | null>(null);

  clearCreateError() {
    this.createError.set(null);
  }

  createPrompt(request: SystemPromptRequest): Observable<SystemPrompt> {
    this.isCreating.set(true);
    this.createError.set(null);

    return this.api.createPrompt(request).pipe(
      tap(() => {
        this.isCreating.set(false);
        this.reloadActivePrompts();
      }),
      catchError((err) => {
        this.createError.set(this.createPromptErrorMessage(err));
        this.isCreating.set(false);
        throw err;
      }),
    );
  }

  archivePrompt(id: string): Observable<SystemPrompt> {
    this.isArchiving.set(true);
    this.deleteError.set(null);

    return this.api.archivePrompt(id).pipe(
      tap((archived) => {
        this.isArchiving.set(false);
        this.reloadActivePrompts();
        if (this.selectedFamilyId() === archived.familyId) {
          this.reloadHistory();
        }
      }),
      catchError((err) => {
        const msg = err.error?.message || 'Failed to archive prompt.';
        this.deleteError.set(msg);
        this.isArchiving.set(false);
        throw err;
      }),
    );
  }

  private createPromptErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Could not connect to the backend server. Please make sure it is running and try again.';
      }

      if (typeof error.error?.message === 'string') {
        return error.error.message;
      }

      if (error.status >= 500) {
        return 'The backend server could not create the prompt. Please try again in a moment.';
      }

      if (error.status >= 400) {
        return 'The prompt could not be saved. Please check the fields and try again.';
      }
    }

    return 'Failed to create prompt. Please try again.';
  }
}
