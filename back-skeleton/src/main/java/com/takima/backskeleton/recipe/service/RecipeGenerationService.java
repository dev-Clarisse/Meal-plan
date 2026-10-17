package com.takima.backskeleton.recipe.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.takima.backskeleton.OllamaClient.OllamaClient;
import com.takima.backskeleton.ingredient.Ingredient;
import com.takima.backskeleton.ingredient.IngredientRepository;
import com.takima.backskeleton.recipe.dtos.AiRecipe;
import com.takima.backskeleton.recipe.dtos.GenerateRecipeRequest;
import com.takima.backskeleton.recipe.dtos.RecipeDraftResponse;
import com.takima.backskeleton.recipe.exception.RecipeGenerationException;



@Service
public class RecipeGenerationService {

    private final OllamaClient ollama;
    private final IngredientRepository ingredientRepository;

    public RecipeGenerationService(OllamaClient ollama, IngredientRepository ingredientRepository) {
        this.ollama = ollama;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public RecipeDraftResponse generate(GenerateRecipeRequest request) {
        List<Ingredient> known = ingredientRepository.findAll(Sort.by("name"));
        Map<String, Ingredient> byName = known.stream()
                .collect(Collectors.toMap(i -> normalize(i.getName()), Function.identity()));

        AiRecipe ai = ollama.generate(request.prompt(), request.servings(),
                known.stream().map(Ingredient::getName).toList());

        if (ai.title() == null || ai.title().isBlank() || ai.ingredients() == null) {
            throw new RecipeGenerationException("Recette générée incomplète");
        }

        List<RecipeDraftResponse.DraftLine> lines = ai.ingredients().stream()
                .filter(l -> l.name() != null && l.quantity() != null
                        && l.quantity().signum() > 0 && l.unit() != null)
                .map(l -> {
                    Ingredient match = find(byName, l.name());
                    return new RecipeDraftResponse.DraftLine(
                            match != null ? match.getId() : null,
                            match != null ? match.getName() : l.name().trim(),
                            l.quantity(), l.unit());
                })
                .toList();

        if (lines.isEmpty()) {
            throw new RecipeGenerationException("Aucun ingrédient exploitable");
        }
        return new RecipeDraftResponse(ai.title().trim(), Math.max(ai.servings(), 1),
                ai.instructions(), lines);
    }

    private static Ingredient find(Map<String, Ingredient> byName, String raw) {
        String n = normalize(raw);
        Ingredient hit = byName.get(n);
        if (hit == null && n.endsWith("s")) {
            hit = byName.get(n.substring(0, n.length() - 1)); // pluriel simple
        }
        return hit;
    }

    private static String normalize(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
