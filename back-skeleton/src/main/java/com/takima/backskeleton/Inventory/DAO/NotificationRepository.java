package com.takima.backskeleton.Inventory.DAO;

import com.takima.backskeleton.Inventory.models.Notification;
import com.takima.backskeleton.Inventory.models.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadIsFalse(UUID userId);

    Optional<Notification> findByInventoryIdAndType(UUID inventoryId, NotificationType type);

    boolean existsByInventoryIdAndType(UUID inventoryId, NotificationType type);
}