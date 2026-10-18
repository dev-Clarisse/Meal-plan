package com.takima.backskeleton.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.integration.RecipeProvider;
import com.takima.backskeleton.integration.RecipeProvider.RecipeLine;
import com.takima.backskeleton.integration.RecipeProvider.RecipeView;
import com.takima.backskeleton.integration.StockProvider;
import com.takima.backskeleton.models.Meal;

@Service
public class MissingIngredientsService {
    private final RecipeProvider recipeProvider;
    private final StockProvider stockProvider;

    public MissingIngredientsService(RecipeProvider recipeProvider, StockProvider stockProvider) {
        this.recipeProvider = recipeProvider;
        this.stockProvider = stockProvider;
    }

    public List<MissingIngredientDto> compute(List<Meal> meals, UUID userId) {
        // Temps 1 : besoins, additionnés sur TOUS les repas avant de toucher au stock
        Map<Long, BigDecimal> needs = new LinkedHashMap<>();
        Map<Long, RecipeLine> info = new LinkedHashMap<>(); // nom + unité de base

        for (Meal meal : meals) {
            RecipeView recipe = recipeProvider.getRecipe(meal.getRecipeId(), userId);
            BigDecimal factor = BigDecimal.valueOf(meal.getServings())
                    .divide(BigDecimal.valueOf(recipe.servings()), 6, RoundingMode.HALF_UP);
            for (RecipeLine line : recipe.lines()) {
                needs.merge(line.ingredientId(), line.quantity().multiply(factor), BigDecimal::add);
                info.putIfAbsent(line.ingredientId(), line);
            }
        }

        // Temps 2 : stock disponible (périmés déjà exclus par le fournisseur)
        Map<Long, BigDecimal> stock = stockProvider.getAvailableStock(userId);

        // Temps 3 et 4 : différence, puis une ligne par manquant
        List<MissingIngredientDto> missing = new ArrayList<>();
        needs.forEach((ingredientId, needed) -> {
            BigDecimal available = stock.getOrDefault(ingredientId, BigDecimal.ZERO);
            BigDecimal diff = needed.subtract(available);
            if (diff.signum() > 0) {
                RecipeLine line = info.get(ingredientId);
                missing.add(new MissingIngredientDto(ingredientId, line.name(),
                        diff.setScale(3, RoundingMode.HALF_UP), line.baseUnit()));
            }
        });
        return missing;
    }
}