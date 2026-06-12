import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ZardButtonComponent } from '@/shared/button/button.component';
import { ZardBadgeComponent } from '@/shared/badge/badge.component';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideFileText, lucideMoon, lucidePlay, lucideSun } from '@ng-icons/lucide';
import { ZardDarkMode } from '@/zard/services';

@Component({
  selector: 'evl-header',
  imports: [RouterLink, RouterLinkActive, NgIcon, ZardButtonComponent, ZardBadgeComponent],
  providers: [
    provideIcons({
      lucideFileText,
      lucidePlay,
      lucideSun,
      lucideMoon,
    }),
  ],

  template: `
    <header
      class="sticky top-0 z-40 border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/75">
      <div class="mx-auto flex h-14 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <a
          routerLink="/"
          class="flex items-center gap-2 rounded-md outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
          aria-label="EvalSuite home">
          <div
            class="flex h-8 w-8 items-center justify-center rounded-lg border bg-card text-card-foreground shadow-sm">
            <ng-icon name="lucideFileText" aria-hidden="true" />
          </div>

          <span class="text-sm font-semibold tracking-tight text-foreground"> EvalSuite </span>

          <z-badge zType="secondary" class="ml-1 h-5 px-1.5 text-[10px] font-medium">
            v0.1
          </z-badge>
        </a>

        <div class="flex items-center gap-2">
          <nav class="hidden items-center gap-1 md:flex" aria-label="Main navigation">
            <a
              routerLink="/system-prompts"
              routerLinkActive="bg-accent text-accent-foreground"
              z-button
              zSize="lg"
              zType="ghost">
              System Prompts
            </a>

            <a
              routerLink="/datasets"
              routerLinkActive="bg-accent text-accent-foreground"
              z-button
              zSize="lg"
              zType="ghost">
              Datasets
            </a>

            <a
              routerLink="/runs"
              routerLinkActive="bg-accent text-accent-foreground"
              z-button
              zType="ghost"
              zSize="lg">
              <ng-icon name="lucidePlay" aria-hidden="true" />
              Runs
            </a>
          </nav>

          <button
            z-button
            zType="ghost"
            zSize="icon"
            type="button"
            class="h-9 w-9"
            (click)="toggleTheme()"
            [attr.aria-label]="themeLabel()"
            [title]="themeLabel()">
            @if (isDark()) {
              <ng-icon name="lucideSun" aria-hidden="true" />
            } @else {
              <ng-icon name="lucideMoon" aria-hidden="true" />
            }
          </button>
        </div>
      </div>
    </header>
  `,
})
export class HeaderComponent {
  private readonly themeService = inject(ZardDarkMode);
  readonly isDark = computed(() => this.themeService.currentTheme() === 'dark');

  readonly themeLabel = computed(() =>
    this.isDark() ? 'Switch to light theme' : 'Switch to dark theme',
  );

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
