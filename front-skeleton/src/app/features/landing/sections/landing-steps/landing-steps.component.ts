import { Component } from '@angular/core';
import { RevealDirective } from '../../../../shared/directives/reveal.directive';

interface Step {
  number: string;
  title: string;
  text: string;
}

@Component({
  selector: 'app-landing-steps',
  standalone: true,
  imports: [RevealDirective],
  templateUrl: './landing-steps.component.html',
  styleUrl: './landing-steps.component.scss',
})
export class LandingStepsComponent {
  readonly steps: Step[] = [
    {
      number: '01',
      title: 'Je range mon frigo',
      text: 'Chaque aliment est noté avec sa quantité et sa date de péremption. Une alerte vous prévient avant qu\u2019il ne soit trop tard.',
    },
    {
      number: '02',
      title: 'Je planifie ma semaine',
      text: 'Je compose mes repas jour par jour à partir de mes recettes, en déplaçant les plats d\u2019un jour à l\u2019autre.',
    },
    {
      number: '03',
      title: 'Je cuisine sans rien jeter',
      text: 'Mijoté calcule ce qui manque et prépare la liste de courses. On n\u2019achète que le nécessaire.',
    },
  ];
}
