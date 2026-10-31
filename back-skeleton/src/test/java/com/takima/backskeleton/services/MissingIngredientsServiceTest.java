package com.takima.backskeleton.services;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.integration.RecipeProvider;
import com.takima.backskeleton.integration.RecipeProvider.RecipeLine;
import com.takima.backskeleton.integration.RecipeProvider.RecipeView;
import com.takima.backskeleton.integration.StockProvider;
import com.takima.backskeleton.models.Meal;
import com.takima.backskeleton.models.MealType;

class MissingIngredientsServiceTest {
    private static final UUID USER = UUID.randomUUID();

    // Chaque recette : 4 portions, 4 œufs
    private final RecipeProvider recipes = (id, user) -> new RecipeView(id, "Recette " + id, 4,
            List.of(new RecipeLine(1L, "Œufs", new BigDecimal("4"), "pièce")));

    private final List<Meal> twoMeals = List.of(
            new Meal(1L, DayOfWeek.MONDAY, MealType.LUNCH, 4),
            new Meal(2L, DayOfWeek.TUESDAY, MealType.DINNER, 4));

    private MissingIngredientsService serviceWithStock(Map<Long, BigDecimal> stock) {
        StockProvider provider = user -> stock;
        return new MissingIngredientsService(recipes, provider);
    }

    @Test
    void eggsScenario_8_needed_6_in_stock_gives_2_missing() {
        List<MissingIngredientDto> result =
                serviceWithStock(Map.of(1L, new BigDecimal("6"))).compute(twoMeals, USER);

        assertEquals(1, result.size());
        assertEquals(0, new BigDecimal("2").compareTo(result.get(0).quantity()));
        assertEquals("Œufs", result.get(0).name());
    }

    @Test
    void enoughStock_gives_empty_list() {
        List<MissingIngredientDto> result =
                serviceWithStock(Map.of(1L, new BigDecimal("10"))).compute(twoMeals, USER);
        assertTrue(result.isEmpty());
    }

    @Test
    void ingredientAbsentFromStock_is_entirely_missing() {
        List<MissingIngredientDto> result = serviceWithStock(Map.of()).compute(twoMeals, USER);
        assertEquals(0, new BigDecimal("8").compareTo(result.get(0).quantity()));
    }

    @Test
    void servingsAreScaled_2_servings_of_a_4_servings_recipe_needs_half() {
        List<Meal> oneSmallMeal = List.of(new Meal(1L, DayOfWeek.MONDAY, MealType.LUNCH, 2));
        List<MissingIngredientDto> result = serviceWithStock(Map.of()).compute(oneSmallMeal, USER);
        assertEquals(0, new BigDecimal("2").compareTo(result.get(0).quantity()));
    }
}