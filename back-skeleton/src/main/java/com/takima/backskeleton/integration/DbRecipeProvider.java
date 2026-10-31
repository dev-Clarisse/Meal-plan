package com.takima.backskeleton.integration;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.takima.backskeleton.common.unit.Unit;
import com.takima.backskeleton.recipe.Recipe;
import com.takima.backskeleton.recipe.RecipeRepository;

@Component
public class DbRecipeProvider implements RecipeProvider {
    private final RecipeRepository recipeRepository;

    public DbRecipeProvider(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeView getRecipe(Long recipeId, UUID userId) {
        Recipe recipe = recipeRepository.findDetailedById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found: " + recipeId));
        List<RecipeLine> lines = recipe.getIngredients().stream()
                .map(ri -> new RecipeLine(
                        ri.getIngredient().getId(),
                        ri.getIngredient().getName(),
                        ri.getUnit().toBaseUnit(ri.getQuantity()),
                        baseUnitLabel(ri.getUnit())))
                .toList();
        return new RecipeView(recipe.getId(), recipe.getTitle(), recipe.getServings(), lines);
    }

    private static String baseUnitLabel(Unit unit) {
        return switch (unit.getFamily()) {
            case MASS -> "g";
            case VOLUME -> "mL";
            case COUNT -> "pièce";
        };
    }
}