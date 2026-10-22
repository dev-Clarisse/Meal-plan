import { Component } from '@angular/core';
import { RevealDirective } from '../../../../shared/directives/reveal.directive';

@Component({
  selector: 'app-landing-name',
  standalone: true,
  imports: [RevealDirective],
  templateUrl: './landing-name.component.html',
  styleUrl: './landing-name.component.scss',
})
export class LandingNameComponent {}
