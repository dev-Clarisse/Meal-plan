package com.takima.backskeleton.integration;
import java.util.UUID;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrat avec Leticia. Les quantités sont DÉJÀ converties en unité de base (g, mL ou pièce)
 * et valent pour le nombre de portions de la recette.
 */
public interface RecipeProvider {
    RecipeView getRecipe(Long recipeId, UUID userId);

    record RecipeLine(Long ingredientId, String name, BigDecimal quantity, String baseUnit) {
    }

    record RecipeView(Long id, String title, int servings, List<RecipeLine> lines) {
    }
}
