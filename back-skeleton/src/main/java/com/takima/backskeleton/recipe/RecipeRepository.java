package com.takima.backskeleton.recipe;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("select r from Recipe r where :title is null or lower(r.title) like lower(concat('%', :title, '%'))")
    Page<Recipe> search(@Param("title") String title, Pageable pageable);

    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredient"})
    @Query("select r from Recipe r where r.id = :id")
    Optional<Recipe> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredient"})
    @Query("select distinct r from Recipe r where r.id in :ids")
    java.util.List<Recipe> findAllDetailedByIdIn(@Param("ids") java.util.List<Long> ids);
}
