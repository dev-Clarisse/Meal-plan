export type Unit = 'G' | 'KG' | 'ML' | 'L' | 'TSP' | 'TBSP' | 'PIECE';

export interface RecipeIngredient {
  ingredientId: number;
  ingredientName: string;
  quantity: number;
  unit: Unit;
}

export interface Recipe {
  id: number;
  title: string;
  servings: number;
  instructions: string | null;
  ingredients: RecipeIngredient[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface RecipeIngredientRequest {
  ingredientId: number;
  quantity: number;
  unit: Unit;
}

export interface RecipeRequest {
  title: string;
  servings: number;
  instructions: string;
  ingredients: RecipeIngredientRequest[];
}

export interface RecipeDraftLine {
  ingredientId: number | null;
  ingredientName: string;
  quantity: number;
  unit: Unit;
}

export interface RecipeDraft {
  title: string;
  servings: number;
  instructions: string;
  ingredients: RecipeDraftLine[];
}
