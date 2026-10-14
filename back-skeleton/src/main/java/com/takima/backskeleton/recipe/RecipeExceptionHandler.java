package com.takima.backskeleton.recipe;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RecipeController.class)
public class RecipeExceptionHandler {

    @ExceptionHandler(RecipeNotFoundException.class)
    public ProblemDetail handleRecipeNotFound(RecipeNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IngredientNotFoundException.class)
    public ProblemDetail handleIngredientNotFound(IngredientNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }
}
