package com.takima.backskeleton.controllers;

import com.takima.backskeleton.DTO.AddMealRequest;
import com.takima.backskeleton.DTO.MealDto;
import com.takima.backskeleton.DTO.MealPlanDto;
import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.DTO.MoveMealRequest;
import com.takima.backskeleton.services.CurrentUserService;
import com.takima.backskeleton.services.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("api/v1")
@RestController
public class MealPlanController {
    private final MealPlanService mealPlanService;
    private final CurrentUserService currentUser;

    public MealPlanController(MealPlanService mealPlanService, CurrentUserService currentUser) {
        this.mealPlanService = mealPlanService;
        this.currentUser = currentUser;
    }

    @Operation(summary = "Récupère (ou crée) le plan de la semaine contenant la date donnée")
    @GetMapping("/meal-plans/{week}")
    public MealPlanDto getWeek(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
                               @AuthenticationPrincipal Jwt jwt) {
        return mealPlanService.getOrCreate(currentUser.idOf(jwt.getSubject()), week);
    }

    @Operation(summary = "Ajoute un repas au plan")
    @PostMapping("/meal-plans/{id}/meals")
    public MealDto addMeal(@PathVariable Long id,
                           @Valid @RequestBody AddMealRequest req,
                           @AuthenticationPrincipal Jwt jwt) {
        return mealPlanService.addMeal(id, currentUser.idOf(jwt.getSubject()), req);
    }

    @Operation(summary = "Déplace un repas (jour et type de repas)")
    @PatchMapping("/meals/{id}/move")
    public MealDto move(@PathVariable Long id,
                        @Valid @RequestBody MoveMealRequest req,
                        @AuthenticationPrincipal Jwt jwt) {
        return mealPlanService.moveMeal(id, currentUser.idOf(jwt.getSubject()), req);
    }

    @Operation(summary = "Calcule les ingrédients manquants du plan")
    @GetMapping("/meal-plans/{id}/missing-ingredients")
    public List<MissingIngredientDto> missing(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return mealPlanService.getMissingIngredients(id, currentUser.idOf(jwt.getSubject()));
    }
}