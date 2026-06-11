import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './shared/layout/header';
import { FooterComponent } from './shared/layout/footer';

@Component({
  selector: 'evl-root',
  templateUrl: './app.html',
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
})
export class App {
  protected readonly title = signal('eval-suite-frontend');
}
