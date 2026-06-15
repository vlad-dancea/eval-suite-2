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
import { RunGridCard } from './cards/run-grid-card';

@Component({
  selector: 'evl-runs',
  imports: [
    ZardDividerComponent,
    NgIcon,
    ZardButtonComponent,
    SkeletonCardComponent,
    ZardCardComponent,
    CreateRunModal,
    RunGridCard,
  ],
  providers: [
    provideIcons({ lucidePlus, lucideCircleQuestionMark, lucideTriangleAlert, lucideRefreshCw }),
  ],
  templateUrl: './runs.component.html',
})
export class RunsComponent {
  private readonly store = inject(RunsStore);

  protected runs = this.store.runs;
  protected isLoading = this.store.isLoadingRuns;
  protected error = this.store.runsError;

  protected reloadRuns() {
    this.store.reloadRuns();
  }

  protected shouldShowCreateRunModal = signal(false);

  protected openCreateRunModal() {
    this.shouldShowCreateRunModal.set(true);
  }

  protected closeCreateRunModal() {
    this.shouldShowCreateRunModal.set(false);
  }
}
