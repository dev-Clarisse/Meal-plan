package com.takima.backskeleton.DTO;

import java.time.Instant;
import java.util.List;

// empty = true quand rien ne manque : le frontend peut afficher "Votre stock suffit"
public record ShoppingListDto(Long id, Instant generatedAt, boolean empty, List<ShoppingItemDto> items) {
}