package com.takima.backskeleton.recipe;

import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        int servings,
        String instructions,
        List<RecipeIngredientResponse> ingredients) {

    public static RecipeResponse from(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getServings(),
                recipe.getInstructions(),
                recipe.getIngredients().stream().map(RecipeIngredientResponse::from).toList());
    }
}
