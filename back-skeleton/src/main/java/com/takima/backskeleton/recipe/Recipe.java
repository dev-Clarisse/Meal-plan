package com.takima.backskeleton.recipe;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private int servings;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    protected Recipe() {
    }

    public Recipe(String title, int servings, String instructions) {
        this.title = title;
        this.servings = servings;
        this.instructions = instructions;
    }

    public void update(String title, int servings, String instructions) {
        this.title = title;
        this.servings = servings;
        this.instructions = instructions;
    }

    public void replaceIngredients(List<RecipeIngredient> newIngredients) {
        this.ingredients.clear();
        this.ingredients.addAll(newIngredients);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getServings() {
        return servings;
    }

    public String getInstructions() {
        return instructions;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}
