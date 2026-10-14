package com.takima.backskeleton.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RecipeRequest(
        @NotBlank @Size(max = 150) String title,
        @Min(1) int servings,
        String instructions,
        @NotEmpty @Valid List<RecipeIngredientRequest> ingredients) {
}
