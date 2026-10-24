import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/landing/landing.component').then((m) => m.LandingComponent),
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },

  {
    path: 'inventory',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/inventory/inventory.component').then((m) => m.InventoryComponent),
  },

  {
    path: 'recettes',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/recipes/recipe-list/recipe-list.component').then((m) => m.RecipeListComponent),
  },
  {
    path: 'recettes/nouvelle',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/recipes/recipe-form/recipe-form.component').then((m) => m.RecipeFormComponent),
  },
  {
    path: 'recettes/:id/modifier',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/recipes/recipe-form/recipe-form.component').then((m) => m.RecipeFormComponent),
  },
  {
    path: 'recettes/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/recipes/recipe-detail/recipe-detail.component').then((m) => m.RecipeDetailComponent),
  },

  { path: '**', redirectTo: 'login' },
];