package com.takima.backskeleton.recipe.dtos;

import java.math.BigDecimal;
import java.util.List;

import com.takima.backskeleton.common.unit.Unit;

public record RecipeDraftResponse(
        String title, int servings, String instructions, List<DraftLine> ingredients) {

    public record DraftLine(Long ingredientId, String ingredientName,
                            BigDecimal quantity, Unit unit) {}
}


