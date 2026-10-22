import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { RevealDirective } from '../../../../shared/directives/reveal.directive';

@Component({
  selector: 'app-landing-closing',
  standalone: true,
  imports: [RouterLink, RevealDirective],
  templateUrl: './landing-closing.component.html',
  styleUrl: './landing-closing.component.scss',
})
export class LandingClosingComponent {
  readonly isLoggedIn = toSignal(inject(AuthService).isLoggedIn$, { initialValue: false });
}
