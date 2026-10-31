package com.takima.backskeleton.DTO;

import java.math.BigDecimal;

public record MissingIngredientDto(Long ingredientId, String name, BigDecimal quantity, String unit) {
}