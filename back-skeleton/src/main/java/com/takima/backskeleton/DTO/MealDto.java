package com.takima.backskeleton.DTO;

import java.time.DayOfWeek;

import com.takima.backskeleton.models.MealType;

public record MealDto(Long id, Long recipeId, DayOfWeek dayOfWeek, MealType mealType, int servings) {
}