package com.takima.backskeleton.models;

import java.time.DayOfWeek;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meals")
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    // Simple colonne pour l'instant : la vraie relation vers Recipe viendra avec le code de Leticia
    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(nullable = false)
    private int servings;

    public Meal() {
    }

    public Meal(Long recipeId, DayOfWeek dayOfWeek, MealType mealType, int servings) {
        this.recipeId = recipeId;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.servings = servings;
    }

    public void moveTo(DayOfWeek day, MealType type) {
        this.dayOfWeek = day;
        this.mealType = type;
    }

    public Long getId() {
        return id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public MealType getMealType() {
        return mealType;
    }

    public int getServings() {
        return servings;
    }
}