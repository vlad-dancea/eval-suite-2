import { Routes } from '@angular/router';
import { SystemPromptsComponent } from './system-prompts/system-prompts.component';
import { DatasetsComponent } from './datasets/datasets.component';
import { RunsComponent } from './runs/runs.component';

export const routes: Routes = [
  { path: '', redirectTo: 'system-prompts', pathMatch: 'full' },
  { path: 'system-prompts', component: SystemPromptsComponent },
  { path: 'datasets', component: DatasetsComponent },
  { path: 'runs', component: RunsComponent },
];
