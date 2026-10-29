package com.takima.backskeleton.DTO;

import com.takima.backskeleton.models.ShoppingItem;
import com.takima.backskeleton.models.ShoppingList;

public class ShoppingListMapper {
    public static ShoppingItemDto toDto(ShoppingItem item) {
        return new ShoppingItemDto(item.getId(), item.getIngredientId(), item.getIngredientName(),
                item.getQuantity(), item.getUnit(), item.isPurchased());
    }

    public static ShoppingListDto toDto(ShoppingList list) {
        return new ShoppingListDto(list.getId(), list.getGeneratedAt(), list.getItems().isEmpty(),
                list.getItems().stream().map(ShoppingListMapper::toDto).toList());
    }
}