package com.takima.backskeleton.recipe.dtos;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateRecipeRequest(
        @NotBlank @Size(max = 300) String prompt,
        @Min(1) Integer servings) {
}
