import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'evl-root',
  templateUrl: './app.html',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
})
export class App {
  protected readonly title = signal('eval-suite-frontend');
}
