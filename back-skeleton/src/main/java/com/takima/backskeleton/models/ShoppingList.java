package com.takima.backskeleton.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_lists",
        uniqueConstraints = @UniqueConstraint(columnNames = {"meal_plan_id"}))
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Une seule liste par plan : on la met à jour au lieu d'en créer une nouvelle
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingItem> items = new ArrayList<>();

    public ShoppingList() {
    }

    public ShoppingList(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
        this.generatedAt = Instant.now();
    }

    public void addItem(ShoppingItem item) {
        items.add(item);
        item.setShoppingList(this);
    }

    public void markGeneratedNow() {
        this.generatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public List<ShoppingItem> getItems() {
        return items;
    }
}