package com.takima.backskeleton.DTO;

import java.math.BigDecimal;

public record ShoppingItemDto(Long id, Long ingredientId, String name, BigDecimal quantity,
                              String unit, boolean purchased) {
}