package com.takima.backskeleton.recipe.dtos;

import java.math.BigDecimal;
import java.util.List;

import com.takima.backskeleton.common.unit.Unit;

public record AiRecipe(String title, int servings, String instructions,
                       List<AiIngredient> ingredients) {
    public record AiIngredient(String name, BigDecimal quantity, Unit unit) {}
}
