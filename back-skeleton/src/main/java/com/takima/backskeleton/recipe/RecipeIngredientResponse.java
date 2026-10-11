package com.takima.backskeleton.recipe;

import com.takima.backskeleton.common.unit.Unit;
import java.math.BigDecimal;

public record RecipeIngredientResponse(Long ingredientId, String ingredientName, BigDecimal quantity, Unit unit) {

    public static RecipeIngredientResponse from(RecipeIngredient line) {
        return new RecipeIngredientResponse(
                line.getIngredient().getId(),
                line.getIngredient().getName(),
                line.getQuantity(),
                line.getUnit());
    }
}
