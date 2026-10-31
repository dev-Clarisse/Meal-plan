package com.takima.backskeleton.DAO;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.takima.backskeleton.models.ShoppingItem;

@Repository
public interface ShoppingItemDao extends JpaRepository<ShoppingItem, Long> {
    // Vérifie que l'élément appartient bien à l'utilisateur connecté
    @Query("SELECT i FROM ShoppingItem i WHERE i.id = :id AND i.shoppingList.mealPlan.userId = :userId")
    Optional<ShoppingItem> findByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);
}
