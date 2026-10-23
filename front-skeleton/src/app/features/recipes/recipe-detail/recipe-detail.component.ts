import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Recipe } from '../models/recipe.model';
import { BackLinkComponent } from '../../../shared/back-link/back-link.component';
import { ConfirmService } from '../../../shared/confirm-dialog/confirm.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { RecipeService } from '../services/recipe.service';

@Component({
  selector: 'app-recipe-detail',
  standalone: true,
  imports: [BackLinkComponent, RouterLink],
  templateUrl: './recipe-detail.component.html',
  styleUrl: './recipe-detail.component.scss',
})
export class RecipeDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly confirmService = inject(ConfirmService);
  private readonly toast = inject(ToastService);
  private readonly recipeService = inject(RecipeService);

  readonly recipe = signal<Recipe | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.recipeService.findById(id).subscribe({
      next: (recipe) => {
        this.recipe.set(recipe);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.status === 404 ? 'Recette introuvable.' : 'Erreur de chargement.');
        this.loading.set(false);
      },
    });
  }

  async remove(): Promise<void> {
    const current = this.recipe();
    if (!current) {
      return;
    }
    const confirmed = await this.confirmService.ask({
      title: 'Supprimer la recette ?',
      message: `« ${current.title} » sera définitivement supprimée.`,
      confirmLabel: 'Supprimer',
      tone: 'danger',
    });
    if (confirmed) {
      this.recipeService.delete(current.id).subscribe({
        next: () => {
          this.toast.success('Recette supprimée.');
          this.router.navigate(['/recettes']);
        },
        error: () => this.toast.error('Impossible de supprimer la recette.'),
      });
    }
  }
}
