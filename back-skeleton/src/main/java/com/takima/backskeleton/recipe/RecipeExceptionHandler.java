package com.takima.backskeleton.recipe;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.takima.backskeleton.recipe.exception.RecipeGenerationException;

@RestControllerAdvice(assignableTypes = RecipeController.class)
public class RecipeExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RecipeExceptionHandler.class);

    @ExceptionHandler(RecipeNotFoundException.class)
    public ProblemDetail handleRecipeNotFound(RecipeNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IngredientNotFoundException.class)
    public ProblemDetail handleIngredientNotFound(IngredientNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }

    @ExceptionHandler(RecipeGenerationException.class)
    public ResponseEntity<Map<String, String>> handle(RecipeGenerationException e) {
        log.error("Génération de recette échouée", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", e.getMessage()));
    }
}
