package com.takima.backskeleton.Inventory.DTO;

import com.takima.backskeleton.Inventory.models.Unit;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InventoryResponse(
    UUID id,
    String name,
    BigDecimal quantity,
    Unit unit,
    LocalDate expiryDate,
    ExpiryStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public enum ExpiryStatus { OK, EXPIRING_SOON, EXPIRED, NO_DATE }
}