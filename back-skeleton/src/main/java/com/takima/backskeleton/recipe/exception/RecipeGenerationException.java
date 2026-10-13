package com.takima.backskeleton.recipe.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class RecipeGenerationException extends RuntimeException {
    public RecipeGenerationException(String message, Throwable cause) { super(message, cause); }
    public RecipeGenerationException(String message) { super(message); }
}
