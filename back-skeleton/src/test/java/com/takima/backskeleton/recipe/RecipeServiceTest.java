package com.takima.backskeleton.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.takima.backskeleton.common.unit.Unit;
import com.takima.backskeleton.ingredient.Ingredient;
import com.takima.backskeleton.ingredient.IngredientRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void createsRecipeWithIngredients() {
        Ingredient egg = new Ingredient("Egg", Unit.PIECE);
        ReflectionTestUtils.setField(egg, "id", 1L);
        when(ingredientRepository.findAllById(List.of(1L))).thenReturn(List.of(egg));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeRequest request = new RecipeRequest(" Omelette ", 2, "Cook",
                List.of(new RecipeIngredientRequest(1L, new BigDecimal("4"), Unit.PIECE)));

        RecipeResponse response = recipeService.create(request);

        assertThat(response.title()).isEqualTo("Omelette");
        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).quantity()).isEqualByComparingTo("4");
    }

    @Test
    void rejectsUnknownIngredient() {
        when(ingredientRepository.findAllById(List.of(999L))).thenReturn(List.of());

        RecipeRequest request = new RecipeRequest("Bad", 1, null,
                List.of(new RecipeIngredientRequest(999L, BigDecimal.ONE, Unit.G)));

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(IngredientNotFoundException.class);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void throwsWhenRecipeIsNotFound() {
        when(recipeRepository.findDetailedById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.findById(42L))
                .isInstanceOf(RecipeNotFoundException.class);
    }

    @Test
    void throwsWhenDeletingUnknownRecipe() {
        when(recipeRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> recipeService.delete(42L))
                .isInstanceOf(RecipeNotFoundException.class);
        verify(recipeRepository, never()).deleteById(any());
    }
}
