package com.takima.backskeleton.DTO;

import java.time.DayOfWeek;

import com.takima.backskeleton.models.MealType;

import jakarta.validation.constraints.NotNull;

public record MoveMealRequest(@NotNull DayOfWeek dayOfWeek, @NotNull MealType mealType) {
}