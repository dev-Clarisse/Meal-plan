import { Component, Injector, OnInit, afterNextRender, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Ingredient } from '../models/ingredient.model';
import { Recipe, RecipeRequest, Unit } from '../models/recipe.model';
import { IngredientService } from '../services/ingredient.service';
import { RecipeService } from '../services/recipe.service';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss',
})
export class RecipeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly recipeService = inject(RecipeService);
  private readonly ingredientService = inject(IngredientService);
  private readonly injector = inject(Injector);

  readonly units: Unit[] = ['G', 'KG', 'ML', 'L', 'TSP', 'TBSP', 'PIECE'];
  readonly ingredients = signal<Ingredient[]>([]);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);
  readonly aiPrompt = signal('');
  readonly generating = signal(false);
  readonly missing = signal<{ name: string; unit: Unit }[]>([]);
  readonly creatingName = signal<string | null>(null);

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
      next: (saved) => this.router.navigate(['/recettes', saved.id]),
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
      pendingName: [''],
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

  generate(): void {
    console.log('generate clicked', this.aiPrompt());
    const prompt = this.aiPrompt().trim();
    if (!prompt || this.generating()) return;

    this.generating.set(true);
    this.error.set(null);
    this.missing.set([]);

    this.recipeService.generate(prompt, this.form.controls.servings.value).subscribe({
      next: (draft) => {
        this.form.patchValue({
          title: draft.title,
          servings: draft.servings,
          instructions: draft.instructions,
        });
        this.lines.clear();
        draft.ingredients.forEach((l) => {
          const group = this.newLine();
          group.patchValue({
            ingredientId: l.ingredientId ?? 0,
            quantity: l.quantity,
            unit: l.unit,
            pendingName: l.ingredientId === null ? l.ingredientName : '',
          });
          this.lines.push(group);
        });

        const seen = new Set<string>();
        this.missing.set(
          draft.ingredients
            .filter((l) => l.ingredientId === null)
            .filter((l) => {
              const key = l.ingredientName.toLowerCase();
              if (seen.has(key)) return false;
              seen.add(key);
              return true;
            })
            .map((l) => ({ name: l.ingredientName, unit: l.unit })),
        );
        this.generating.set(false);
      },
      error: () => {
        this.error.set('La génération a échoué. Réessaie ou saisis la recette à la main.');
        this.generating.set(false);
      },
    });
  }

  createMissing(item: { name: string; unit: Unit }): void {
    if (this.creatingName()) return;
    const name = item.name.charAt(0).toUpperCase() + item.name.slice(1); // 'egg' -> 'Egg', comme ton seed
    this.creatingName.set(item.name);

    this.ingredientService.create(name, item.unit).subscribe({
      next: (created) => {
        this.ingredients.update((list) =>
          [...list, created].sort((a, b) => a.name.localeCompare(b.name)),
        );
        this.resolve(item.name, created.id);
      },
      error: (err) => {
        if (err.status === 409) {
          // existe déjà (casse différente, etc.) : on recharge la liste et on le réutilise
          this.ingredientService.search().subscribe((list) => {
            this.ingredients.set(list);
            const found = list.find((i) => i.name.toLowerCase() === name.toLowerCase());
            if (found) {
              this.resolve(item.name, found.id);
            } else {
              this.creatingName.set(null);
            }
          });
        } else {
          this.error.set(`Impossible de créer « ${name} ».`);
          this.creatingName.set(null);
        }
      },
    });
  }

  private resolve(pendingName: string, ingredientId: number): void {
    const key = pendingName.toLowerCase();
    this.missing.update((list) => list.filter((m) => m.name.toLowerCase() !== key));
    this.creatingName.set(null);

    // On attend que le nouvel <option> soit rendu, sinon le <select> n'affiche rien
    afterNextRender(
      () => {
        this.lines.controls.forEach((c) => {
          if (c.value.ingredientId === 0 && c.value.pendingName?.toLowerCase() === key) {
            c.patchValue({ ingredientId, pendingName: '' });
          }
        });
      },
      { injector: this.injector },
    );
  }
}
