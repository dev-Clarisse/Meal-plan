package com.takima.backskeleton.ingredient;

import com.takima.backskeleton.common.unit.Unit;

public record IngredientResponse(Long id, String name, Unit defaultUnit) {

    public static IngredientResponse from(Ingredient ingredient) {
        return new IngredientResponse(ingredient.getId(), ingredient.getName(), ingredient.getDefaultUnit());
    }
}
