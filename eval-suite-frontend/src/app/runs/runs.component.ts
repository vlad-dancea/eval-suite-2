import { Component } from '@angular/core';
import { ZardDividerComponent } from '@/shared/divider';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { ZardButtonComponent } from '@/shared/button';
import { lucidePlus } from '@ng-icons/lucide';

@Component({
  selector: 'evl-runs',
  imports: [ZardDividerComponent, NgIcon, ZardButtonComponent],
  providers: [provideIcons({ lucidePlus })],
  templateUrl: './runs.component.html',
})
export class RunsComponent {}
