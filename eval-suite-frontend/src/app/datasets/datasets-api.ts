import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DatasetItem {
  id: string;
  position: number;
  input: string;
  expectedOutput: string;
  inputPreview: string;
  expectedOutputPreview: string;
}

export interface Dataset {
  id: string;
  name: string;
  itemCount: number;
  createdAt: string;
  deletedAt: string | null;
  items: DatasetItem[];
}

export interface DatasetSummary {
  id: string;
  name: string;
  itemCount: number;
  createdAt: string;
}

export interface DatasetItemRequest {
  input: string;
  expectedOutput: string;
}

export interface DatasetRequest {
  name: string;
  items: DatasetItemRequest[];
}

@Service()
export class DatasetsApi {
  private readonly baseUrl = 'http://localhost:8080/api/datasets';
  private readonly http = inject(HttpClient);

  activeDatasets(): string {
    return this.baseUrl;
  }

  datasetDetail(id: string): string {
    return `${this.baseUrl}/${id}`;
  }

  createDataset(request: DatasetRequest): Observable<Dataset> {
    return this.http.post<Dataset>(this.baseUrl, request);
  }

  deleteDataset(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
