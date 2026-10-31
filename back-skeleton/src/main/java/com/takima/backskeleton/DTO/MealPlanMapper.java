package com.takima.backskeleton.DTO;

import com.takima.backskeleton.models.Meal;
import com.takima.backskeleton.models.MealPlan;

public class MealPlanMapper {
    public static MealDto toDto(Meal meal) {
        return new MealDto(meal.getId(), meal.getRecipeId(), meal.getDayOfWeek(),
                meal.getMealType(), meal.getServings());
    }

    public static MealPlanDto toDto(MealPlan plan) {
        return new MealPlanDto(plan.getId(), plan.getWeekStart(),
                plan.getMeals().stream().map(MealPlanMapper::toDto).toList());
    }
}