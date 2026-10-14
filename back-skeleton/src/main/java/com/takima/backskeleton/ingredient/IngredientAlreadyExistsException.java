package com.takima.backskeleton.ingredient;

public class IngredientAlreadyExistsException extends RuntimeException {

    public IngredientAlreadyExistsException(String name) {
        super("Ingredient already exists: " + name);
    }
}
