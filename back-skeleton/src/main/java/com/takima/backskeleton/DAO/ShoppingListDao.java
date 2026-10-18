package com.takima.backskeleton.DAO;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.takima.backskeleton.models.ShoppingList;

@Repository
public interface ShoppingListDao extends JpaRepository<ShoppingList, Long> {
    Optional<ShoppingList> findByMealPlanId(Long mealPlanId);
}