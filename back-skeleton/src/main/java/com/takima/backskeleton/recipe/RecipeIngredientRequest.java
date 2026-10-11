package com.takima.backskeleton.recipe;

import com.takima.backskeleton.common.unit.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RecipeIngredientRequest(
        @NotNull Long ingredientId,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
        @NotNull Unit unit) {
}
