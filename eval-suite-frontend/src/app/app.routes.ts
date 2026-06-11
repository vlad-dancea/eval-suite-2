import { Routes } from '@angular/router';
import { SystemPromptsComponent } from './system-prompts/system-prompts.component';

export const routes: Routes = [
  { path: '', redirectTo: 'system-prompts', pathMatch: 'full' },
  { path: 'system-prompts', component: SystemPromptsComponent },
];
