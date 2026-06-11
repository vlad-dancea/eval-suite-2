import { Component } from '@angular/core';

@Component({
  selector: 'evl-footer',
  template: `
    <footer
      class="bg-slate-950/40 border-t border-slate-900/60 py-6 text-center text-xs text-slate-500">
      <div class="max-w-7xl mx-auto px-4">
        <p>© 2026 EvalSuite. Traceable and repeatable LLM evaluations.</p>
      </div>
    </footer>
  `,
})
export class FooterComponent {}
