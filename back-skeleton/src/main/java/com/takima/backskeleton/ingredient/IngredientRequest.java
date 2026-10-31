package com.takima.backskeleton.ingredient;

import com.takima.backskeleton.common.unit.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IngredientRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Unit defaultUnit) {
}
