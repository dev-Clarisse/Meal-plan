import { Location } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-back-link',
  standalone: true,
  templateUrl: './back-link.component.html',
  styleUrl: './back-link.component.scss',
})
export class BackLinkComponent {
  private readonly location = inject(Location);
  private readonly router = inject(Router);

  @Input() fallback = '/';
  @Input() label = 'Retour';

  goBack(): void {
    const hasHistory = (history.state?.navigationId ?? 0) > 1;
    if (hasHistory) {
      this.location.back();
      return;
    }
    this.router.navigateByUrl(this.fallback);
  }
}
