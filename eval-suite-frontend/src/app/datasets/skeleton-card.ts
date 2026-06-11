import { ZardCardComponent } from '@/shared/card';
import { ZardSkeletonComponent } from '@/shared/skeleton';
import { Component } from '@angular/core';

@Component({
  selector: 'evl-dataset-skeleton-card',
  template: `
    <z-card [zFooterBorder]="true" class="group min-h-56 justify-between">
      <div class="space-y-4">
        <div class="flex items-start justify-between gap-3">
          <z-skeleton class="h-5 w-20" />
          <z-skeleton class="h-5 w-25" />
        </div>

        <z-skeleton class="h-6 w-[50%]" />

        <div class="space-y-2">
          <z-skeleton class="h-4 w-full rounded-md" />
          <z-skeleton class="h-4 w-3/4 rounded-md" />
        </div>
      </div>

      <!-- Card Actions -->
      <div card-footer class="flex w-full items-center justify-between">
        <z-skeleton class="h-5 w-20" />
        <z-skeleton class="h-5 w-20" />
      </div>
    </z-card>
  `,
  imports: [ZardSkeletonComponent, ZardCardComponent],
})
export class DatasetSkeletonCardComponent {}
