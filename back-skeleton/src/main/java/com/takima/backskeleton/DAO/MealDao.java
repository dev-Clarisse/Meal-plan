package com.takima.backskeleton.DAO;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.takima.backskeleton.models.Meal;

@Repository
public interface MealDao extends JpaRepository<Meal, Long> {
    // Vérifie que le repas appartient bien à l'utilisateur connecté
    @Query("SELECT m FROM Meal m WHERE m.id = :id AND m.mealPlan.userId = :userId")
    Optional<Meal> findByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);
}
