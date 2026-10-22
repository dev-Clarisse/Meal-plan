import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Ingredient } from '../models/ingredient.model';
import { Recipe, RecipeRequest, Unit } from '../models/recipe.model';
import { IngredientService } from '../services/ingredient.service';
import { BackLinkComponent } from '../../../shared/back-link/back-link.component';
import { RecipeService } from '../services/recipe.service';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [BackLinkComponent, ReactiveFormsModule, RouterLink],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss',
})
export class RecipeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly recipeService = inject(RecipeService);
  private readonly ingredientService = inject(IngredientService);

  readonly units: Unit[] = ['G', 'KG', 'ML', 'L', 'TSP', 'TBSP', 'PIECE'];
  readonly ingredients = signal<Ingredient[]>([]);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);

  recipeId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    servings: [1, [Validators.required, Validators.min(1)]],
    instructions: [''],
    ingredients: this.fb.array([this.newLine()]),
  });

  get lines(): FormArray {
    return this.form.controls.ingredients;
  }

  ngOnInit(): void {
    this.ingredientService.search().subscribe((list) => this.ingredients.set(list));

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.recipeId = Number(id);
      this.recipeService.findById(this.recipeId).subscribe({
        next: (recipe) => this.fill(recipe),
        error: () => this.error.set('Recette introuvable.'),
      });
    }
  }

  addLine(): void {
    this.lines.push(this.newLine());
  }

  removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: RecipeRequest = {
      title: value.title,
      servings: value.servings,
      instructions: value.instructions,
      ingredients: value.ingredients.map((line: any) => ({
        ingredientId: Number(line.ingredientId),
        quantity: Number(line.quantity),
        unit: line.unit as Unit,
      })),
    };

    this.saving.set(true);
    this.error.set(null);
    const call =
      this.recipeId === null
        ? this.recipeService.create(request)
        : this.recipeService.update(this.recipeId, request);

    call.subscribe({
      next: (saved) => this.router.navigate(['/recettes', saved.id], { replaceUrl: true }),
      error: () => {
        this.error.set("Impossible d'enregistrer la recette.");
        this.saving.set(false);
      },
    });
  }

  private newLine() {
    return this.fb.nonNullable.group({
      ingredientId: [0, [Validators.required, Validators.min(1)]],
      quantity: [1, [Validators.required, Validators.min(0.0001)]],
      unit: ['G', Validators.required],
    });
  }

  private fill(recipe: Recipe): void {
    this.form.patchValue({
      title: recipe.title,
      servings: recipe.servings,
      instructions: recipe.instructions ?? '',
    });
    this.lines.clear();
    recipe.ingredients.forEach((line) => {
      const group = this.newLine();
      group.patchValue({
        ingredientId: line.ingredientId,
        quantity: line.quantity,
        unit: line.unit,
      });
      this.lines.push(group);
    });
  }
}
