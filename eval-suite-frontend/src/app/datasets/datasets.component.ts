import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideArchive,
  lucideDatabase,
  lucideEye,
  lucidePlus,
  lucideRefreshCw,
  lucideTriangleAlert,
} from '@ng-icons/lucide';

import { ZardBadgeComponent } from '@/shared/badge';
import { ZardButtonComponent } from '@/shared/button';
import { ZardCardComponent } from '@/shared/card';
import { ZardDividerComponent } from '@/shared/divider';

import { DatasetSummary } from './datasets-api';
import { DatasetsStore } from './datasets-store';
import { DatasetSkeletonCardComponent } from './skeleton-card';
import { DatasetCreateModal } from './modals/create-modal';
import { DatasetViewModal } from './modals/view-modal';
import { DatasetDeleteModal } from './modals/delete-modal';

@Component({
  selector: 'evl-datasets',
  imports: [
    CommonModule,
    NgIcon,
    ZardBadgeComponent,
    ZardButtonComponent,
    ZardCardComponent,
    ZardDividerComponent,
    DatasetSkeletonCardComponent,
    DatasetCreateModal,
    DatasetViewModal,
    DatasetDeleteModal,
  ],
  providers: [
    provideIcons({
      lucideArchive,
      lucideDatabase,
      lucideEye,
      lucidePlus,
      lucideRefreshCw,
      lucideTriangleAlert,
    }),
  ],
  templateUrl: './datasets.component.html',
})
export class DatasetsComponent {
  protected readonly store = inject(DatasetsStore);

  protected readonly selectedDataset = signal<DatasetSummary | null>(null);

  protected readonly showCreateModal = signal<boolean>(false);
  protected readonly showViewModal = signal<boolean>(false);
  protected readonly showDeleteModal = signal<boolean>(false);

  openCreateModal() {
    this.showCreateModal.set(true);
  }

  closeCreateModal() {
    this.showCreateModal.set(false);
  }

  openViewModal(dataset: DatasetSummary) {
    this.selectedDataset.set(dataset);
    this.store.selectDataset(dataset.id);
    this.showViewModal.set(true);
  }

  closeViewModal() {
    this.showViewModal.set(false);
    this.selectedDataset.set(null);
    this.store.selectDataset(null);
  }

  openDeleteModal(dataset: DatasetSummary) {
    this.selectedDataset.set(dataset);
    this.showDeleteModal.set(true);
  }

  closeDeleteModal() {
    this.showDeleteModal.set(false);
    this.selectedDataset.set(null);
  }

  confirmDeleteDataset() {
    const dataset = this.selectedDataset();
    if (!dataset) {
      return;
    }

    this.store.deleteDataset(dataset.id).subscribe({
      next: () => {
        this.closeDeleteModal();
      },
      error: (err) => {
        console.error(err);
      },
    });
  }
}
