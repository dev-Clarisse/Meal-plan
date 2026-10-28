import { Page, Recipe, RecipeRequest } from '../models/recipe.model';

export function buildRecipe(overrides: Partial<Recipe> = {}): Recipe {
  return {
    id: 1,
    title: 'Crepes',
    servings: 4,
    instructions: 'Mix and cook.',
    ingredients: [{ ingredientId: 1, ingredientName: 'Flour', quantity: 250, unit: 'G' }],
    ...overrides,
  };
}

export function buildRecipeRequest(overrides: Partial<RecipeRequest> = {}): RecipeRequest {
  return {
    title: 'Crepes',
    servings: 4,
    instructions: 'Mix and cook.',
    ingredients: [{ ingredientId: 1, quantity: 250, unit: 'G' }],
    ...overrides,
  };
}

export function buildPage(content: Recipe[], overrides: Partial<Page<Recipe>> = {}): Page<Recipe> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 6,
    first: true,
    last: true,
    ...overrides,
  };
}
