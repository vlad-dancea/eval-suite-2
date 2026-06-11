import { inject, signal, computed, Service } from '@angular/core';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { DatasetsApi, Dataset, DatasetSummary, DatasetRequest } from './datasets-api';

@Service()
export class DatasetsStore {
  private readonly api = inject(DatasetsApi);

  // === Active Datasets ===
  private readonly activeDatasetsResource = httpResource<DatasetSummary[]>(
    () => {
      return {
        url: this.api.activeDatasets(),
      };
    },
    { defaultValue: [] },
  );

  readonly activeDatasets = computed(() => this.activeDatasetsResource.value());
  readonly isLoadingActiveDatasets = computed(() => this.activeDatasetsResource.isLoading());
  readonly activeDatasetsError = computed(() => this.activeDatasetsResource.error());

  reloadActiveDatasets() {
    this.activeDatasetsResource.reload();
  }

  // === Detail (selected dataset with its items) ===
  readonly selectedDatasetId = signal<string | null>(null);

  private readonly detailResource = httpResource<Dataset | undefined>(
    () => {
      const id = this.selectedDatasetId();
      if (!id) {
        return undefined;
      }

      return {
        url: this.api.datasetDetail(id),
      };
    },
    { defaultValue: undefined },
  );

  readonly selectedDataset = computed(() => this.detailResource.value());
  readonly isLoadingDetail = computed(() => this.detailResource.isLoading());
  readonly detailError = computed(() => this.detailResource.error());

  selectDataset(id: string | null) {
    this.selectedDatasetId.set(id);
  }

  // === CREATE / DELETE ===
  readonly isCreating = signal<boolean>(false);
  readonly createError = signal<string | null>(null);
  readonly isDeleting = signal<boolean>(false);
  readonly deleteError = signal<string | null>(null);

  clearCreateError() {
    this.createError.set(null);
  }

  createDataset(request: DatasetRequest): Observable<Dataset> {
    this.isCreating.set(true);
    this.createError.set(null);

    return this.api.createDataset(request).pipe(
      tap(() => {
        this.isCreating.set(false);
        this.reloadActiveDatasets();
      }),
      catchError((err) => {
        this.createError.set(this.createDatasetErrorMessage(err));
        this.isCreating.set(false);
        throw err;
      }),
    );
  }

  deleteDataset(id: string): Observable<void> {
    this.isDeleting.set(true);
    this.deleteError.set(null);

    return this.api.deleteDataset(id).pipe(
      tap(() => {
        this.isDeleting.set(false);
        this.reloadActiveDatasets();
        if (this.selectedDatasetId() === id) {
          this.selectDataset(null);
        }
      }),
      catchError((err) => {
        const msg = err.error?.message || 'Failed to delete dataset.';
        this.deleteError.set(msg);
        this.isDeleting.set(false);
        throw err;
      }),
    );
  }

  private createDatasetErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Could not connect to the backend server. Please make sure it is running and try again.';
      }

      if (typeof error.error?.message === 'string') {
        return error.error.message;
      }

      if (error.status >= 500) {
        return 'The backend server could not create the dataset. Please try again in a moment.';
      }

      if (error.status >= 400) {
        return 'The dataset could not be saved. Please check the fields and try again.';
      }
    }

    return 'Failed to create dataset. Please try again.';
  }
}
