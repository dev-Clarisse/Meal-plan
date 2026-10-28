export type DayOfWeek =
  | 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY'
  | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

export interface Meal {
  id: number;
  recipeId: number;
  dayOfWeek: DayOfWeek;
  mealType: MealType;
  servings: number;
}

export interface MealPlan {
  id: number;
  weekStart: string;
  meals: Meal[];
}

export interface AddMealRequest {
  recipeId: number;
  dayOfWeek: DayOfWeek;
  mealType: MealType;
  servings: number;
}

export interface MoveMealRequest {
  dayOfWeek: DayOfWeek;
  mealType: MealType;
}

export interface MissingIngredient {
  ingredientId: number;
  name: string;
  quantity: number;
  unit: string;
}

export interface ShoppingItem {
  id: number;
  ingredientId: number;
  name: string;
  quantity: number;
  unit: string;
  purchased: boolean;
}

export interface ShoppingList {
  id: number;
  generatedAt: string;
  empty: boolean;
  items: ShoppingItem[];
}

// Le backend plafonne la taille de page à 50 : suffisant pour lister les recettes du planning
export const RECIPE_PAGE_SIZE = 50;

export const DAYS: { value: DayOfWeek; label: string }[] = [
  { value: 'MONDAY', label: 'Lundi' },
  { value: 'TUESDAY', label: 'Mardi' },
  { value: 'WEDNESDAY', label: 'Mercredi' },
  { value: 'THURSDAY', label: 'Jeudi' },
  { value: 'FRIDAY', label: 'Vendredi' },
  { value: 'SATURDAY', label: 'Samedi' },
  { value: 'SUNDAY', label: 'Dimanche' },
];

export const MEAL_TYPES: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: 'Petit-déjeuner' },
  { value: 'LUNCH', label: 'Déjeuner' },
  { value: 'DINNER', label: 'Dîner' },
];