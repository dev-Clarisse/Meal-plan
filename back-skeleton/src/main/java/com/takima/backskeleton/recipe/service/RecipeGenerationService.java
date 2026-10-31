package com.takima.backskeleton.recipe.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.takima.backskeleton.OllamaClient.OllamaClient;
import com.takima.backskeleton.common.unit.Unit;
import com.takima.backskeleton.ingredient.Ingredient;
import com.takima.backskeleton.ingredient.IngredientRepository;
import com.takima.backskeleton.recipe.dtos.AiRecipe;
import com.takima.backskeleton.recipe.dtos.GenerateRecipeRequest;
import com.takima.backskeleton.recipe.dtos.RecipeDraftResponse;
import com.takima.backskeleton.recipe.exception.RecipeGenerationException;

@Service
public class RecipeGenerationService {

    private static final Pattern SERVINGS_IN_PROMPT = Pattern.compile(
            "(\\d+)\\s*(?:personnes?|pers\\b|portions?|convives?|parts?)\\b|\\bpour\\s+(\\d+)\\s*\\.?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern TITLE_SERVINGS_SUFFIX = Pattern.compile(
            "\\s*[-,(]?\\s*pour\\s+\\d+\\s*(?:personnes?|pers\\b\\.?|portions?|convives?|parts?)?\\s*\\)?\\s*\\.?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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
                .collect(Collectors.toMap(i -> normalize(i.getName()), Function.identity(), (a, b) -> a));

        Integer requested = requestedServings(request);

        // Le catalogue n'est pas envoyé au modèle : il y piochait des ingrédients sans rapport avec le plat.
        AiRecipe ai = ollama.generate(request.prompt(), requested);

        if (ai.title() == null || ai.title().isBlank() || ai.ingredients() == null) {
            throw new RecipeGenerationException("Recette générée incomplète");
        }

        int servings = requested != null ? requested : Math.max(ai.servings(), 1);
        BigDecimal scale = ai.servings() > 0
                ? BigDecimal.valueOf(servings).divide(BigDecimal.valueOf(ai.servings()), 6, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        List<RecipeDraftResponse.DraftLine> lines = ai.ingredients().stream()
                .filter(l -> l.name() != null && l.quantity() != null
                        && l.quantity().signum() > 0 && l.unit() != null)
                .map(l -> {
                    Ingredient match = find(byName, l.name());
                    if (match == null) {
                        return new RecipeDraftResponse.DraftLine(null, l.name().trim(),
                                clean(l.quantity().multiply(scale)), l.unit());
                    }
                    return new RecipeDraftResponse.DraftLine(match.getId(), match.getName(),
                            quantityInCatalogUnit(l, match, scale, servings), match.getDefaultUnit());
                })
                .toList();

        if (lines.isEmpty()) {
            throw new RecipeGenerationException("Aucun ingrédient exploitable");
        }

        String rawInstructions = ai.instructions();
        String safeInstructions = "";
        if (rawInstructions != null) {
            safeInstructions = rawInstructions.toString().trim();
            if (safeInstructions.startsWith("{") || safeInstructions.startsWith("[")) {
                safeInstructions = "1. " + ai.title() + "\n(Instructions générées sous un format non standard)";
            }
        }

        return new RecipeDraftResponse(cleanTitle(ai.title()), servings, safeInstructions, lines);
    }

    // Le nombre écrit dans la demande passe avant le champ Portions du formulaire, qui vaut 1 par défaut.
    private static Integer requestedServings(GenerateRecipeRequest request) {
        Matcher matcher = SERVINGS_IN_PROMPT.matcher(request.prompt());
        if (matcher.find()) {
            int servings = Integer.parseInt(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (servings >= 1) {
                return servings;
            }
        }
        return request.servings();
    }

    private static BigDecimal quantityInCatalogUnit(AiRecipe.AiIngredient line, Ingredient match,
            BigDecimal scale, int servings) {
        Unit target = match.getDefaultUnit();
        if (line.unit().getFamily() == target.getFamily()) {
            return clean(line.unit().convertTo(line.quantity().multiply(scale), target));
        }
        // Unité inconvertible (1 PIECE de poulet) : estimation par portion, à relire dans le brouillon.
        BigDecimal perServing = switch (target.getFamily()) {
            case MASS -> new BigDecimal("100");
            case VOLUME -> new BigDecimal("15");
            case COUNT -> BigDecimal.ONE;
        };
        Unit base = switch (target.getFamily()) {
            case MASS -> Unit.G;
            case VOLUME -> Unit.ML;
            case COUNT -> Unit.PIECE;
        };
        return clean(base.convertTo(perServing.multiply(BigDecimal.valueOf(servings)), target));
    }

    private static BigDecimal clean(BigDecimal quantity) {
        BigDecimal rounded = quantity.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        return rounded.scale() < 0 ? rounded.setScale(0) : rounded;
    }

    private static String cleanTitle(String title) {
        return TITLE_SERVINGS_SUFFIX.matcher(title.trim()).replaceAll("").trim();
    }

    private static Ingredient find(Map<String, Ingredient> byName, String raw) {
        return byName.get(normalize(raw));
    }

    // Ignore la casse, les accents et les pluriels : « Pâte » et « oeufs » retrouvent « Pâtes » et « Oeuf ».
    private static String normalize(String s) {
        String lower = s.trim().toLowerCase(Locale.ROOT).replace("œ", "oe");
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("s\\b", "");
    }
}
