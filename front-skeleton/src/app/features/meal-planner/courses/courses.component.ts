import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Recipe } from '../../recipes/models/recipe.model';
import { RecipeService } from '../../recipes/services/recipe.service';
import { DAYS, MEAL_TYPES, Meal, MealPlan, RECIPE_PAGE_SIZE } from '../meal-planner.model';
import { MealPlannerService } from '../meal-planner.service';
import { ShoppingListComponent } from '../shopping-list/shopping-list.component';
import { addDays, longDate, toIso, weekFromParam } from '../week';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [RouterLink, ShoppingListComponent],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.scss'],
})
export class CoursesComponent implements OnInit {
  private readonly service = inject(MealPlannerService);
  private readonly recipeService = inject(RecipeService);
  private readonly router = inject(Router);

  readonly longDate = longDate;
  recipes: Recipe[] = [];

  weekStart = weekFromParam(inject(ActivatedRoute).snapshot.queryParamMap.get('semaine'));
  plan?: MealPlan;
  error = '';

  ngOnInit(): void {
    this.recipeService.search('', 0, RECIPE_PAGE_SIZE).subscribe({
      next: (page) => (this.recipes = page.content),
    });
    this.loadWeek();
  }

  get weekParam(): string {
    return toIso(this.weekStart);
  }

  loadWeek(): void {
    this.error = '';
    this.service.getWeek(this.weekParam).subscribe({
      next: (plan) => (this.plan = plan),
      error: () => (this.error = 'Impossible de charger la semaine (le serveur tourne-t-il ?)'),
    });
  }

  shiftWeek(weeks: number): void {
    this.weekStart = addDays(this.weekStart, 7 * weeks);
    this.router.navigate([], { queryParams: { semaine: this.weekParam }, replaceUrl: true });
    this.loadWeek();
  }

  // Repas triés dans l'ordre de la semaine, puis du petit-déjeuner au dîner
  sortedMeals(): Meal[] {
    const day = (m: Meal) => DAYS.findIndex((d) => d.value === m.dayOfWeek);
    const type = (m: Meal) => MEAL_TYPES.findIndex((t) => t.value === m.mealType);
    return [...(this.plan?.meals ?? [])].sort((a, b) => day(a) - day(b) || type(a) - type(b));
  }

  slotLabel(meal: Meal): string {
    const day = DAYS.find((d) => d.value === meal.dayOfWeek)?.label ?? '';
    const type = MEAL_TYPES.find((t) => t.value === meal.mealType)?.label ?? '';
    return `${day} · ${type}`;
  }

  recipeTitle(recipeId: number): string {
    return this.recipes.find((r) => r.id === recipeId)?.title ?? `Recette ${recipeId}`;
  }
}
