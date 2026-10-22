import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Ingredient } from '../models/ingredient.model';
import { Recipe, RecipeRequest, Unit } from '../models/recipe.model';
import { IngredientService } from '../services/ingredient.service';
import { BackLinkComponent } from '../../../shared/back-link/back-link.component';
import { ConfirmService } from '../../../shared/confirm-dialog/confirm.service';
import { ToastService } from '../../../shared/toast/toast.service';
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
  private readonly confirmService = inject(ConfirmService);
  private readonly toast = inject(ToastService);
  private readonly recipeService = inject(RecipeService);
  private readonly ingredientService = inject(IngredientService);

  readonly units: Unit[] = ['G', 'KG', 'ML', 'L', 'TSP', 'TBSP', 'PIECE'];
  readonly ingredients = signal<Ingredient[]>([]);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);
  readonly aiPrompt = signal('');
  readonly generating = signal(false);

  /** Ingrédients suggérés par l'IA et absents de la base (créés à l'enregistrement). */
  readonly missing = signal<{ name: string; unit: Unit }[]>([]);

  readonly sameId = (a: unknown, b: unknown) => Number(a) === Number(b);

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
    this.ingredientService.search().subscribe((list) => {
      this.ingredients.set(list);
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.recipeId = Number(id);
        this.recipeService.findById(this.recipeId).subscribe({
          next: (recipe) => this.fill(recipe),
          error: () => this.error.set('Recette introuvable.'),
        });
      }
    });
  }

  addLine(): void {
    this.lines.push(this.newLine());
  }

  removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
      this.refreshMissing();
    }
  }

  async submit(): Promise<void> {
    if (this.saving()) return;

    const invalid =
      this.form.controls.title.invalid ||
      this.form.controls.servings.invalid ||
      this.lines.controls.some((c) => this.isLineInvalid(c));

    if (invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    try {
      await this.resolvePending(this.lines.controls.filter((c) => this.isPending(c)));
    } catch {
      this.error.set('Impossible de créer les nouveaux ingrédients.');
      this.saving.set(false);
      return;
    }

    await this.save();
  }

  private async save(): Promise<void> {
    const editing = this.recipeId !== null;
    const confirmed = await this.confirmService.ask({
      title: editing ? 'Enregistrer les modifications ?' : 'Créer la recette ?',
      message: editing
        ? 'La recette sera mise à jour avec ces informations.'
        : 'La nouvelle recette sera ajoutée à votre carnet.',
      confirmLabel: editing ? 'Enregistrer' : 'Créer',
    });

    if (!confirmed) {
      this.saving.set(false);
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

    this.error.set(null);
    const call =
      this.recipeId === null
        ? this.recipeService.create(request)
        : this.recipeService.update(this.recipeId, request);

    call.subscribe({
      next: (saved) => {
        this.toast.success(editing ? 'Recette modifiée.' : 'Recette créée.');
        this.router.navigate(['/recettes', saved.id], { replaceUrl: true });
      },
      error: () => {
        this.toast.error("Impossible d'enregistrer la recette.");
        this.error.set("Impossible d'enregistrer la recette.");
        this.saving.set(false);
      },
    });
  }


  private isPending(c: AbstractControl): boolean {
    return c.value.ingredientId === 0 && !!c.value.pendingName?.trim();
  }

  private isLineInvalid(c: AbstractControl): boolean {
    if (this.isPending(c)) {
      return !!(c.get('quantity')?.invalid || c.get('unit')?.invalid);
    }
    return c.invalid;
  }

  private async resolvePending(pending: AbstractControl[]): Promise<void> {
    if (pending.length === 0) return;

    const byName = new Map<string, { name: string; unit: Unit }>();
    for (const c of pending) {
      const name = c.value.pendingName.trim();
      const key = name.toLowerCase();
      if (!byName.has(key)) byName.set(key, { name, unit: c.value.unit as Unit });
    }

    const idsByKey = new Map<string, number>();
    await Promise.all(
      [...byName.entries()].map(async ([key, item]) => {
        idsByKey.set(key, await this.ensureIngredient(item.name, item.unit));
      }),
    );

    for (const c of pending) {
      const key = c.value.pendingName.trim().toLowerCase();
      c.patchValue({ ingredientId: idsByKey.get(key), pendingName: '' });
    }
    this.missing.set([]);
  }

  private async ensureIngredient(rawName: string, unit: Unit): Promise<number> {
    const name = rawName.charAt(0).toUpperCase() + rawName.slice(1);
    const key = name.toLowerCase();

    const existing = this.ingredients().find((i) => i.name.toLowerCase() === key);
    if (existing) return existing.id;

    try {
      const created = await firstValueFrom(this.ingredientService.create(name, unit));
      this.ingredients.update((list) =>
        [...list, created].sort((a, b) => a.name.localeCompare(b.name)),
      );
      return created.id;
    } catch (err: any) {
      if (err.status === 409) {
        const list = await firstValueFrom(this.ingredientService.search());
        this.ingredients.set(list);
        const found = list.find((i) => i.name.toLowerCase() === key);
        if (found) return found.id;
      }
      throw err;
    }
  }

  private refreshMissing(): void {
    const seen = new Set<string>();
    const list: { name: string; unit: Unit }[] = [];
    for (const c of this.lines.controls) {
      if (!this.isPending(c)) continue;
      const name = c.value.pendingName.trim();
      const key = name.toLowerCase();
      if (seen.has(key)) continue;
      seen.add(key);
      list.push({ name, unit: c.value.unit as Unit });
    }
    this.missing.set(list);
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

        this.refreshMissing();
        this.generating.set(false);
      },
      error: () => {
        this.error.set('La génération a échoué. Réessaie ou saisis la recette à la main.');
        this.generating.set(false);
      },
    });
  }
}