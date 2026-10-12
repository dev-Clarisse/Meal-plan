import { NgModule } from "@angular/core"
import { RouterModule, Routes } from "@angular/router"
import { HomeComponent } from "home/home.component"

const routes: Routes = [
  { path: "", component: HomeComponent },
  {
    path: "recettes",
    loadComponent: () =>
      import("./features/recipes/recipe-list/recipe-list.component").then(
        (m) => m.RecipeListComponent
      ),
  },
  {
    path: "recettes/nouvelle",
    loadComponent: () =>
      import("./features/recipes/recipe-form/recipe-form.component").then(
        (m) => m.RecipeFormComponent
      ),
  },
  {
    path: "recettes/:id/modifier",
    loadComponent: () =>
      import("./features/recipes/recipe-form/recipe-form.component").then(
        (m) => m.RecipeFormComponent
      ),
  },
  {
    path: "recettes/:id",
    loadComponent: () =>
      import("./features/recipes/recipe-detail/recipe-detail.component").then(
        (m) => m.RecipeDetailComponent
      ),
  },
  { path: "**", redirectTo: "" },
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
