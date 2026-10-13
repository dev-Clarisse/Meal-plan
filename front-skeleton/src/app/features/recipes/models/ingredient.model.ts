import { Unit } from './recipe.model';

export interface Ingredient {
  id: number;
  name: string;
  defaultUnit: Unit;
}
