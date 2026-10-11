package com.takima.backskeleton.ingredient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = IngredientController.class)
public class IngredientExceptionHandler {

    @ExceptionHandler(IngredientAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(IngredientAlreadyExistsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
