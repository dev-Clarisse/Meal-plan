package com.takima.backskeleton.recipe;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(Long id) {
        super("Recipe not found: " + id);
    }
}
