package com.takima.backskeleton.ingredient;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Ingredient> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
