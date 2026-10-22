import { Component } from '@angular/core';
import { RevealDirective } from '../../../../shared/directives/reveal.directive';

interface Line {
  label: string;
  value: string;
}

@Component({
  selector: 'app-landing-example',
  standalone: true,
  imports: [RevealDirective],
  templateUrl: './landing-example.component.html',
  styleUrl: './landing-example.component.scss',
})
export class LandingExampleComponent {
  readonly needs: Line[] = [
    { label: 'Omelette', value: '4 œufs' },
    { label: 'Gâteau', value: '4 œufs' },
  ];
}
