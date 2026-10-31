package com.takima.backskeleton.DTO;

import java.time.LocalDate;
import java.util.List;

public record MealPlanDto(Long id, LocalDate weekStart, List<MealDto> meals) {
}