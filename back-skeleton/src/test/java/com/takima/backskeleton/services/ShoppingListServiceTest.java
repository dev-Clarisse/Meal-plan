package com.takima.backskeleton.services;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.models.ShoppingItem;
import com.takima.backskeleton.models.ShoppingList;

class ShoppingListServiceTest {
    private MissingIngredientDto eggs(String qty) {
        return new MissingIngredientDto(1L, "Œufs", new BigDecimal(qty), "pièce");
    }

    private MissingIngredientDto flour(String qty) {
        return new MissingIngredientDto(3L, "Farine", new BigDecimal(qty), "g");
    }

    @Test
    void firstGeneration_creates_one_item_per_missing_ingredient() {
        ShoppingList list = new ShoppingList();
        ShoppingListService.synchronize(list, List.of(eggs("2"), flour("500")));
        assertEquals(2, list.getItems().size());
    }

    @Test
    void regeneration_creates_no_duplicates_and_updates_quantity() {
        ShoppingList list = new ShoppingList();
        ShoppingListService.synchronize(list, List.of(eggs("2")));
        ShoppingListService.synchronize(list, List.of(eggs("4")));

        assertEquals(1, list.getItems().size());
        assertEquals(0, new BigDecimal("4").compareTo(list.getItems().get(0).getQuantity()));
    }

    @Test
    void regeneration_keeps_purchased_flag() {
        ShoppingList list = new ShoppingList();
        ShoppingListService.synchronize(list, List.of(eggs("2")));
        list.getItems().get(0).setPurchased(true);

        ShoppingListService.synchronize(list, List.of(eggs("3")));

        assertEquals(1, list.getItems().size());
        assertTrue(list.getItems().get(0).isPurchased());
    }

    @Test
    void unpurchased_item_that_is_no_longer_missing_is_removed() {
        ShoppingList list = new ShoppingList();
        ShoppingListService.synchronize(list, List.of(eggs("2"), flour("500")));

        ShoppingListService.synchronize(list, List.of(eggs("2")));

        assertEquals(1, list.getItems().size());
        assertEquals(1L, list.getItems().get(0).getIngredientId());
    }

    @Test
    void purchased_item_that_is_no_longer_missing_is_kept() {
        ShoppingList list = new ShoppingList();
        ShoppingListService.synchronize(list, List.of(flour("500")));
        ShoppingItem flourItem = list.getItems().get(0);
        flourItem.setPurchased(true);

        ShoppingListService.synchronize(list, List.of());

        assertEquals(1, list.getItems().size());
    }

    @Test
    void nothing_missing_gives_empty_list() {
        ShoppingList list = new ShoppingList();
        ShoppingListService.synchronize(list, List.of());
        assertTrue(list.getItems().isEmpty());
    }
}