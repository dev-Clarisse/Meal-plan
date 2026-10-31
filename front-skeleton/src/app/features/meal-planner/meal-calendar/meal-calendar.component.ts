import { CdkDrag, CdkDragDrop, CdkDropList, CdkDropListGroup } from '@angular/cdk/drag-drop';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Recipe } from '../../recipes/models/recipe.model';
import { RecipeService } from '../../recipes/services/recipe.service';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  DAYS, DayOfWeek, MEAL_TYPES, Meal, MealPlan, MealType, MissingIngredient, RECIPE_PAGE_SIZE,
} from '../meal-planner.model';
import { MealPlannerService } from '../meal-planner.service';
import { QuantityPipe } from '../quantity.pipe';
import { addDays, longDate, shortDate, toIso, weekFromParam } from '../week';

export interface Slot {
  day: DayOfWeek;
  type: MealType;
}

@Component({
  selector: 'app-meal-calendar',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CdkDropListGroup, CdkDropList, CdkDrag, QuantityPipe],
  templateUrl: './meal-calendar.component.html',
  styleUrls: ['./meal-calendar.component.scss'],
})
export class MealCalendarComponent implements OnInit {
  private readonly service = inject(MealPlannerService);
  private readonly recipeService = inject(RecipeService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly days = DAYS;
  readonly mealTypes = MEAL_TYPES;
  readonly longDate = longDate;
  readonly shortDate = shortDate;
  recipes: Recipe[] = [];

  weekStart = weekFromParam(inject(ActivatedRoute).snapshot.queryParamMap.get('semaine'));
  plan?: MealPlan;
  error = '';
  missing: MissingIngredient[] | null = null;

  readonly form = inject(FormBuilder).nonNullable.group({
    recipeId: [0, [Validators.required, Validators.min(1)]],
    dayOfWeek: ['MONDAY' as DayOfWeek, Validators.required],
    mealType: ['LUNCH' as MealType, Validators.required],
    servings: [4, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    this.loadRecipes();
    this.loadWeek();
  }

  loadRecipes(): void {
    this.recipeService.search('', 0, RECIPE_PAGE_SIZE).subscribe({
      next: (page) => {
        this.recipes = page.content;
        if (this.recipes.length > 0 && this.form.controls.recipeId.value === 0) {
          this.form.patchValue({ recipeId: this.recipes[0].id, servings: this.recipes[0].servings });
        }
      },
      error: () => this.toast.error('Impossible de charger les recettes'),
    });
  }

  loadWeek(): void {
    this.error = '';
    this.missing = null;
    this.service.getWeek(toIso(this.weekStart)).subscribe({
      next: (plan) => (this.plan = plan),
      error: () => (this.error = 'Impossible de charger le plan (le serveur tourne-t-il ?)'),
    });
  }

  get weekParam(): string {
    return toIso(this.weekStart);
  }

  shiftWeek(weeks: number): void {
    this.weekStart = addDays(this.weekStart, 7 * weeks);
    // La semaine est gardée dans l'URL pour la retrouver en passant à la page Courses
    this.router.navigate([], { queryParams: { semaine: this.weekParam }, replaceUrl: true });
    this.loadWeek();
  }

  dayDate(index: number): Date {
    return addDays(this.weekStart, index);
  }

  mealsIn(day: DayOfWeek, type: MealType): Meal[] {
    return (this.plan?.meals ?? []).filter((m) => m.dayOfWeek === day && m.mealType === type);
  }

  slot(day: DayOfWeek, type: MealType): Slot {
    return { day, type };
  }

  recipeTitle(recipeId: number): string {
    return this.recipes.find((r) => r.id === recipeId)?.title ?? `Recette ${recipeId}`;
  }

  addMeal(): void {
    if (!this.plan || this.form.invalid) {
      return;
    }
    this.service.addMeal(this.plan.id, this.form.getRawValue()).subscribe({
      next: (meal) => {
        this.plan!.meals = [...this.plan!.meals, meal];
        this.toast.success(`${this.recipeTitle(meal.recipeId)} ajouté au planning`);
        this.refreshMissingIfShown();
      },
      error: () => this.toast.error("Impossible d'ajouter le repas"),
    });
  }

  drop(event: CdkDragDrop<Slot, Slot, Meal>): void {
    const meal = event.item.data;
    const target = event.container.data;
    if (meal.dayOfWeek === target.day && meal.mealType === target.type) {
      return;
    }
    // Mise à jour optimiste : on annule si le serveur refuse
    const previous = { day: meal.dayOfWeek, type: meal.mealType };
    meal.dayOfWeek = target.day;
    meal.mealType = target.type;

    this.service.moveMeal(meal.id, { dayOfWeek: target.day, mealType: target.type }).subscribe({
      next: () => this.refreshMissingIfShown(),
      error: () => {
        meal.dayOfWeek = previous.day;
        meal.mealType = previous.type;
        this.toast.error('Déplacement refusé par le serveur');
      },
    });
  }

  loadMissing(): void {
    if (!this.plan) {
      return;
    }
    this.service.getMissing(this.plan.id).subscribe({
      next: (m) => (this.missing = m),
      error: () => this.toast.error('Calcul des manquants impossible'),
    });
  }

  private refreshMissingIfShown(): void {
    if (this.missing !== null) {
      this.loadMissing();
    }
  }
}
