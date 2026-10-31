package com.takima.backskeleton.DAO;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.takima.backskeleton.models.MealPlan;

@Repository
public interface MealPlanDao extends JpaRepository<MealPlan, Long> {
    Optional<MealPlan> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);

    Optional<MealPlan> findByIdAndUserId(Long id, UUID userId);
}
