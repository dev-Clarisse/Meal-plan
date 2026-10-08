package com.takima.backskeleton.Inventory.DAO;

import com.takima.backskeleton.Inventory.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findAllByUserIdOrderByExpiryDateAsc(UUID userId);

    Optional<Inventory> findByIdAndUserId(UUID id, UUID userId);

    // Pour le scheduler : tous les items avec une date d'expiration <= seuil
    List<Inventory> findAllByExpiryDateLessThanEqual(LocalDate threshold);
}