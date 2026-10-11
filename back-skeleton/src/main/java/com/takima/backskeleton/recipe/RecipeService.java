package com.takima.backskeleton.recipe;

import com.takima.backskeleton.ingredient.Ingredient;
import com.takima.backskeleton.ingredient.IngredientRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponse> search(String title, Pageable pageable) {
        String filter = (title == null || title.isBlank()) ? null : title.trim();
        Page<Recipe> page = recipeRepository.search(filter, pageable);
        List<Long> ids = page.getContent().stream().map(Recipe::getId).toList();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }
        Map<Long, Recipe> detailedById = recipeRepository.findAllDetailedByIdIn(ids).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));
        List<RecipeResponse> content = ids.stream()
                .map(detailedById::get)
                .map(RecipeResponse::from)
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RecipeResponse findById(Long id) {
        return recipeRepository.findDetailedById(id)
                .map(RecipeResponse::from)
                .orElseThrow(() -> new RecipeNotFoundException(id));
    }

    @Transactional
    public RecipeResponse create(RecipeRequest request) {
        Recipe recipe = new Recipe(request.title().trim(), request.servings(), request.instructions());
        recipe.replaceIngredients(buildLines(recipe, request));
        return RecipeResponse.from(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeResponse update(Long id, RecipeRequest request) {
        Recipe recipe = recipeRepository.findDetailedById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));
        recipe.update(request.title().trim(), request.servings(), request.instructions());
        recipe.replaceIngredients(buildLines(recipe, request));
        return RecipeResponse.from(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new RecipeNotFoundException(id);
        }
        recipeRepository.deleteById(id);
    }

    private List<RecipeIngredient> buildLines(Recipe recipe, RecipeRequest request) {
        List<Long> ingredientIds = request.ingredients().stream()
                .map(RecipeIngredientRequest::ingredientId)
                .distinct()
                .toList();
        Map<Long, Ingredient> ingredientsById = ingredientRepository.findAllById(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        return request.ingredients().stream()
                .map(line -> {
                    Ingredient ingredient = ingredientsById.get(line.ingredientId());
                    if (ingredient == null) {
                        throw new IngredientNotFoundException(line.ingredientId());
                    }
                    return new RecipeIngredient(recipe, ingredient, line.quantity(), line.unit());
                })
                .sorted(Comparator.comparing(line -> line.getIngredient().getName()))
                .toList();
    }
}
