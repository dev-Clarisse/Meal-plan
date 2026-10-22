import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LandingClosingComponent } from './sections/landing-closing/landing-closing.component';
import { LandingExampleComponent } from './sections/landing-example/landing-example.component';
import { LandingNameComponent } from './sections/landing-name/landing-name.component';
import { LandingStepsComponent } from './sections/landing-steps/landing-steps.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    RouterLink,
    LandingNameComponent,
    LandingStepsComponent,
    LandingExampleComponent,
    LandingClosingComponent,
  ],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  readonly isLoggedIn = toSignal(inject(AuthService).isLoggedIn$, { initialValue: false });
}
