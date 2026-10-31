package com.takima.backskeleton.DTO;

import java.time.DayOfWeek;

import com.takima.backskeleton.models.MealType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddMealRequest(@NotNull Long recipeId, @NotNull DayOfWeek dayOfWeek,
                             @NotNull MealType mealType, @Positive int servings) {
}