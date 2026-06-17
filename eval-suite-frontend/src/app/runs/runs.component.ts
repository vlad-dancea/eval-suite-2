import { Component, inject, signal } from '@angular/core';
import { ZardDividerComponent } from '@/shared/divider';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { ZardButtonComponent } from '@/shared/button';
import {
  lucideCircleQuestionMark,
  lucidePlus,
  lucideRefreshCw,
  lucideTriangleAlert,
} from '@ng-icons/lucide';
import { RunsStore } from './runs.store';
import { SkeletonCardComponent } from '@/system-prompts/skeleton-card';
import { ZardCardComponent } from '@/shared/card';
import { CreateRunModal } from './modals/create-run-modal';
import { RunDetailModal } from './modals/run-detail-modal';
import { RunGridCard } from './cards/run-grid-card';
import { type Run } from './runs.api';

@Component({
  selector: 'evl-runs',
  imports: [
    ZardDividerComponent,
    NgIcon,
    ZardButtonComponent,
    SkeletonCardComponent,
    ZardCardComponent,
    CreateRunModal,
    RunDetailModal,
    RunGridCard,
  ],
  providers: [
    provideIcons({ lucidePlus, lucideCircleQuestionMark, lucideTriangleAlert, lucideRefreshCw }),
  ],
  templateUrl: './runs.component.html',
})
export class RunsComponent {
  private readonly store = inject(RunsStore);

  protected familyCards = this.store.familyCards;
  protected isLoading = this.store.isLoadingRuns;
  protected error = this.store.runsError;

  protected reloadRuns() {
    this.store.reloadRuns();
  }

  protected shouldShowCreateRunModal = signal(false);
  protected selectedFamilyName = signal('');
  protected shouldShowDetailModal = signal(false);

  protected openCreateRunModal() {
    this.shouldShowCreateRunModal.set(true);
  }

  protected closeCreateRunModal() {
    this.shouldShowCreateRunModal.set(false);
  }

  protected openRunDetail(run: Run) {
    this.selectedFamilyName.set(run.systemPromptName);
    this.store.selectFamily(run.systemPromptFamilyId ?? run.id);
    this.store.selectRun(run.id);
    this.shouldShowDetailModal.set(true);
  }

  protected closeRunDetail() {
    this.shouldShowDetailModal.set(false);
    this.store.selectFamily(null);
  }
}
