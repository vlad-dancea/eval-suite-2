import { ZardCardComponent } from '@/shared/card';
import { ZardSkeletonComponent } from '@/shared/skeleton';
import { Component } from '@angular/core';

@Component({
  selector: 'evl-skeleton-card',
  template: `
    <z-card [zFooterBorder]="true" class="group min-h-72 justify-between">
      <div class="space-y-4">
        <div class="flex items-start justify-between gap-3">
          <z-skeleton class="h-5 w-7.5" />
          <z-skeleton class="h-5 w-25" />
        </div>

        <z-skeleton class="h-6 w-[50%]" />

        <!-- Prompt Preview -->
        <div class="max-h-52 overflow-y-auto">
          <z-skeleton class="h-52 w-full rounded-xl" />
        </div>
      </div>

      <!-- Card Actions -->
      <div card-footer class="flex w-full items-center justify-between">
        <z-skeleton class="h-5 w-25" />
        <z-skeleton class="h-5 w-25" />
      </div>
    </z-card>
  `,
  imports: [ZardSkeletonComponent, ZardCardComponent],
})
export class SkeletonCardComponent {}
