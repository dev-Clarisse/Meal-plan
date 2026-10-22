import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AddMealRequest, Meal, MealPlan, MissingIngredient,
  MoveMealRequest, ShoppingItem, ShoppingList,
} from './meal-planner.model';

@Injectable({ providedIn: 'root' })
export class MealPlannerService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/v1`;

  getWeek(isoDate: string): Observable<MealPlan> {
    return this.http.get<MealPlan>(`${this.baseUrl}/meal-plans/${isoDate}`);
  }

  addMeal(planId: number, req: AddMealRequest): Observable<Meal> {
    return this.http.post<Meal>(`${this.baseUrl}/meal-plans/${planId}/meals`, req);
  }

  moveMeal(mealId: number, req: MoveMealRequest): Observable<Meal> {
    return this.http.patch<Meal>(`${this.baseUrl}/meals/${mealId}/move`, req);
  }

  getMissing(planId: number): Observable<MissingIngredient[]> {
    return this.http.get<MissingIngredient[]>(
      `${this.baseUrl}/meal-plans/${planId}/missing-ingredients`);
  }

  generateShoppingList(planId: number): Observable<ShoppingList> {
    return this.http.post<ShoppingList>(
      `${this.baseUrl}/meal-plans/${planId}/shopping-list`, {});
  }

  getShoppingList(planId: number): Observable<ShoppingList> {
    return this.http.get<ShoppingList>(`${this.baseUrl}/meal-plans/${planId}/shopping-list`);
  }

  setPurchased(itemId: number, purchased: boolean): Observable<ShoppingItem> {
    return this.http.patch<ShoppingItem>(`${this.baseUrl}/shopping-items/${itemId}`, { purchased });
  }
}