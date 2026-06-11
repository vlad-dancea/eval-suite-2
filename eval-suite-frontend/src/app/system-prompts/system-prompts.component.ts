import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SystemPrompt } from './system-prompts-api';
import { SystemPromptsStore } from './system-prompts-store';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { ZardBadgeComponent } from '@/shared/badge';
import {
  lucideArchive,
  lucideCircleQuestionMark,
  lucideHistory,
  lucidePlus,
  lucideRefreshCw,
  lucideSquarePen,
  lucideTriangleAlert,
} from '@ng-icons/lucide';
import { ZardButtonComponent } from '@/shared/button';
import { ZardDividerComponent } from '@/shared/divider';
import { SystemPromptCreateModal } from './modals/create-modal';
import { SystemPromptNewVersionModal } from './modals/new-version-modal';
import { SystemPromptVersionModal } from './modals/version-modal';
import { ZardCardComponent } from '@/shared/card';
import { SkeletonCardComponent } from './skeleton-card';
import { SystemPromptContentModal } from './modals/content-modal';
import { ArchiveConfirmationModal } from './modals/archive-modal';

@Component({
  selector: 'evl-system-prompts',
  standalone: true,
  imports: [
    CommonModule,
    ArchiveConfirmationModal,
    NgIcon,
    SystemPromptContentModal,
    SystemPromptCreateModal,
    SystemPromptNewVersionModal,
    SystemPromptVersionModal,
    ZardBadgeComponent,
    ZardButtonComponent,
    ZardDividerComponent,
    ZardCardComponent,
    SkeletonCardComponent,
  ],
  providers: [
    provideIcons({
      lucideArchive,
      lucideCircleQuestionMark,
      lucideHistory,
      lucidePlus,
      lucideRefreshCw,
      lucideSquarePen,
      lucideTriangleAlert,
    }),
  ],
  templateUrl: './system-prompts.component.html',
})
export class SystemPromptsComponent {
  protected readonly store = inject(SystemPromptsStore);

  protected readonly selectedPrompt = signal<SystemPrompt | null>(null);
  protected readonly selectedFamilyName = signal<string>('');

  protected readonly showHistoryModal = signal<boolean>(false);
  protected readonly showCreateModal = signal<boolean>(false);
  protected readonly showNewVersionModal = signal<boolean>(false);
  protected readonly showContentModal = signal<boolean>(false);
  protected readonly showArchiveModal = signal<boolean>(false);

  openContentModal(prompt: SystemPrompt) {
    this.selectedPrompt.set(prompt);
    this.showContentModal.set(true);
  }

  closeContentModal() {
    this.showContentModal.set(false);
    this.selectedPrompt.set(null);
  }

  openCreateModal() {
    this.selectedPrompt.set(null);
    this.showCreateModal.set(true);
  }

  openNewVersionModal(prompt: SystemPrompt) {
    this.selectedPrompt.set(prompt);
    this.showNewVersionModal.set(true);
  }

  openNewVersionFromHistory(prompt: SystemPrompt) {
    this.closeHistoryModal();
    this.openNewVersionModal(prompt);
  }

  closeCreateModal() {
    this.showCreateModal.set(false);
  }

  closeNewVersionModal() {
    this.showNewVersionModal.set(false);
    this.selectedPrompt.set(null);
  }

  openHistoryModal(prompt: SystemPrompt) {
    this.selectedFamilyName.set(prompt.name);
    this.store.selectFamily(prompt.familyId);
    this.showHistoryModal.set(true);
  }

  closeHistoryModal() {
    this.showHistoryModal.set(false);
    this.store.selectFamily(null);
  }

  openArchiveModal(prompt: SystemPrompt) {
    this.selectedPrompt.set(prompt);
    this.showArchiveModal.set(true);
  }

  closeArchiveModal() {
    this.showArchiveModal.set(false);
    this.selectedPrompt.set(null);
  }

  confirmArchivePrompt() {
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
