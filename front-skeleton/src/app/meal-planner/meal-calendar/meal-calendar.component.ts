import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DAYS, DayOfWeek, FAKE_RECIPES, MEAL_TYPES, Meal, MealPlan, MealType, MissingIngredient } from '../meal-planner.model';
import { MealPlannerService } from '../meal-planner.service';

export interface Slot {
  day: DayOfWeek;
  type: MealType;
}

function toIso(d: Date): string {
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

function mondayOf(d: Date): Date {
  const c = new Date(d);
  c.setDate(c.getDate() - ((c.getDay() + 6) % 7));
  return c;
}

@Component({
  selector: 'app-meal-calendar',
  templateUrl: './meal-calendar.component.html',
  styleUrls: ['./meal-calendar.component.scss'],
})
export class MealCalendarComponent implements OnInit {
  readonly days = DAYS;
  readonly mealTypes = MEAL_TYPES;
  readonly recipes = FAKE_RECIPES;

  weekStart = mondayOf(new Date());
  plan?: MealPlan;
  error = '';
  missing: MissingIngredient[] | null = null;
  form;

  constructor(private service: MealPlannerService, fb: FormBuilder) {
    this.form = fb.nonNullable.group({
      recipeId: [1, Validators.required],
      dayOfWeek: ['MONDAY' as DayOfWeek, Validators.required],
      mealType: ['LUNCH' as MealType, Validators.required],
      servings: [4, [Validators.required, Validators.min(1)]],
    });
  }

  ngOnInit(): void {
    this.loadWeek();
  }

  loadWeek(): void {
    this.error = '';
    this.missing = null;
    this.service.getWeek(toIso(this.weekStart)).subscribe({
      next: (plan) => (this.plan = plan),
      error: () => (this.error = 'Impossible de charger le plan (le serveur tourne-t-il ?)'),
    });
  }

  shiftWeek(weeks: number): void {
    const next = new Date(this.weekStart);
    next.setDate(next.getDate() + 7 * weeks);
    this.weekStart = next;
    this.loadWeek();
  }

  dayDate(index: number): Date {
    const d = new Date(this.weekStart);
    d.setDate(d.getDate() + index);
    return d;
  }

  mealsIn(day: DayOfWeek, type: MealType): Meal[] {
    return (this.plan?.meals ?? []).filter((m) => m.dayOfWeek === day && m.mealType === type);
  }

  slot(day: DayOfWeek, type: MealType): Slot {
    return { day, type };
  }

  trackMeal(_: number, meal: Meal): number {
    return meal.id;
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
        this.refreshMissingIfShown();
      },
      error: () => (this.error = "Impossible d'ajouter le repas"),
    });
  }

  drop(event: CdkDragDrop<Slot, Slot, Meal>): void {
    const meal = event.item.data;
    const target = event.container.data;
    if (meal.dayOfWeek === target.day && meal.mealType === target.type) {
      return;
    }
    const previous = { day: meal.dayOfWeek, type: meal.mealType };
    meal.dayOfWeek = target.day;
    meal.mealType = target.type;

    this.service.moveMeal(meal.id, { dayOfWeek: target.day, mealType: target.type }).subscribe({
      next: () => this.refreshMissingIfShown(),
      error: () => {
        meal.dayOfWeek = previous.day;
        meal.mealType = previous.type;
        this.error = 'Déplacement refusé par le serveur';
      },
    });
  }

  loadMissing(): void {
    if (!this.plan) {
      return;
    }
    this.service.getMissing(this.plan.id).subscribe({
      next: (m) => (this.missing = m),
      error: () => (this.error = 'Calcul des manquants impossible'),
    });
  }

  private refreshMissingIfShown(): void {
    if (this.missing !== null) {
      this.loadMissing();
    }
  }
}