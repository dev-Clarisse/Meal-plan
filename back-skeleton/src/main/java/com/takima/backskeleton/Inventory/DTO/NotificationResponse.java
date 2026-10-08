package com.takima.backskeleton.Inventory.DTO;

import com.takima.backskeleton.Inventory.models.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID inventoryId,
    String inventoryName,
    NotificationType type,
    String message,
    boolean read,
    Instant createdAt
) {}