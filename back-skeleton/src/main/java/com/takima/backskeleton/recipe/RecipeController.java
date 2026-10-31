package com.takima.backskeleton.recipe;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.takima.backskeleton.recipe.dtos.GenerateRecipeRequest;
import com.takima.backskeleton.recipe.dtos.RecipeDraftResponse;
import com.takima.backskeleton.recipe.service.RecipeGenerationService;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private static final int MAX_PAGE_SIZE = 50;

    private final RecipeService recipeService;
    private final RecipeGenerationService recipeGenerationService;

    public RecipeController(RecipeService recipeService, RecipeGenerationService recipeGenerationService) {
        this.recipeService = recipeService;
        this.recipeGenerationService=recipeGenerationService;
    }

    @GetMapping
    public Page<RecipeResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return recipeService.search(title, PageRequest.of(Math.max(page, 0), safeSize, Sort.by("title")));
    }

    @GetMapping("/{id}")
    public RecipeResponse findById(@PathVariable Long id) {
        return recipeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse create(@Valid @RequestBody RecipeRequest request) {
        return recipeService.create(request);
    }

    @PutMapping("/{id}")
    public RecipeResponse update(@PathVariable Long id, @Valid @RequestBody RecipeRequest request) {
        return recipeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recipeService.delete(id);
    }

    @PostMapping("/generate")
    public RecipeDraftResponse generate(@Valid @RequestBody GenerateRecipeRequest request) {
        return recipeGenerationService.generate(request);
    }
}
