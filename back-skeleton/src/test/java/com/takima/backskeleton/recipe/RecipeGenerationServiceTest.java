package com.takima.backskeleton.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.takima.backskeleton.OllamaClient.OllamaClient;
import com.takima.backskeleton.common.unit.Unit;
import com.takima.backskeleton.ingredient.Ingredient;
import com.takima.backskeleton.ingredient.IngredientRepository;
import com.takima.backskeleton.recipe.dtos.AiRecipe;
import com.takima.backskeleton.recipe.dtos.AiRecipe.AiIngredient;
import com.takima.backskeleton.recipe.dtos.GenerateRecipeRequest;
import com.takima.backskeleton.recipe.dtos.RecipeDraftResponse;
import com.takima.backskeleton.recipe.service.RecipeGenerationService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class RecipeGenerationServiceTest {

    @Mock
    private OllamaClient ollama;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeGenerationService service;

    @BeforeEach
    void catalog() {
        when(ingredientRepository.findAll(any(Sort.class))).thenReturn(List.of(
                new Ingredient("Poulet", Unit.G),
                new Ingredient("Pâtes", Unit.G),
                new Ingredient("Crème", Unit.ML)));
    }

    private void modelAnswers(String title, int servings, AiIngredient... lines) {
        when(ollama.generate(any(), any()))
                .thenReturn(new AiRecipe(title, servings, "1. Cuire.", List.of(lines)));
    }

    private static AiIngredient line(String name, String quantity, Unit unit) {
        return new AiIngredient(name, new BigDecimal(quantity), unit);
    }

    @Test
    void servingsWrittenInPromptWinOverFormDefault() {
        modelAnswers("Gratin", 4, line("Pâtes", "400", Unit.G));

        RecipeDraftResponse draft = service.generate(new GenerateRecipeRequest("gratin pour 4 personnes", 1));

        assertThat(draft.servings()).isEqualTo(4);
        verify(ollama).generate(eq("gratin pour 4 personnes"), eq(4));
    }

    @Test
    void formServingsAreUsedWhenPromptHasNone() {
        modelAnswers("Gratin", 3, line("Pâtes", "300", Unit.G));

        assertThat(service.generate(new GenerateRecipeRequest("gratin", 3)).servings()).isEqualTo(3);
    }

    @Test
    void quantitiesAreScaledWhenModelIgnoresRequestedServings() {
        modelAnswers("Gratin", 1, line("Pâtes", "100", Unit.G));

        RecipeDraftResponse draft = service.generate(new GenerateRecipeRequest("gratin pour 4 personnes", 1));

        assertThat(draft.ingredients().get(0).quantity()).isEqualByComparingTo("400");
    }

    @Test
    void sameFamilyIsConvertedToCatalogUnit() {
        modelAnswers("Gratin", 4, line("Pâtes", "0.25", Unit.KG));

        RecipeDraftResponse.DraftLine pasta =
                service.generate(new GenerateRecipeRequest("gratin pour 4 personnes", null)).ingredients().get(0);

        assertThat(pasta.unit()).isEqualTo(Unit.G);
        assertThat(pasta.quantity()).isEqualByComparingTo("250");
    }

    @Test
    void incompatibleUnitIsReplacedByEstimateInCatalogUnit() {
        modelAnswers("Gratin", 4, line("Poulet", "1", Unit.PIECE));

        RecipeDraftResponse.DraftLine chicken =
                service.generate(new GenerateRecipeRequest("gratin pour 4 personnes", null)).ingredients().get(0);

        assertThat(chicken.unit()).isEqualTo(Unit.G);
        assertThat(chicken.quantity()).isEqualByComparingTo("400");
    }

    @Test
    void catalogNameIsRecognizedDespiteAccentsAndPlural() {
        modelAnswers("Gratin", 2, line("creme", "80", Unit.ML), line("Pâte", "200", Unit.G));

        RecipeDraftResponse.DraftLine cream =
                service.generate(new GenerateRecipeRequest("gratin", 2)).ingredients().get(0);

        assertThat(cream.ingredientName()).isEqualTo("Crème");
        assertThat(service.generate(new GenerateRecipeRequest("gratin", 2)).ingredients().get(1).ingredientName())
                .isEqualTo("Pâtes");
    }

    @Test
    void servingsAreRemovedFromTitle() {
        modelAnswers("Gratin de pâtes pour 1 personne", 4, line("Pâtes", "400", Unit.G));

        assertThat(service.generate(new GenerateRecipeRequest("gratin pour 4 personnes", 1)).title())
                .isEqualTo("Gratin de pâtes");
    }
}
