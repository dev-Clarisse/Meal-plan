package com.takima.backskeleton.ingredient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.takima.backskeleton.common.unit.Unit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void createsIngredientWhenNameIsFree() {
        when(ingredientRepository.existsByNameIgnoreCase("Flour")).thenReturn(false);
        when(ingredientRepository.save(any(Ingredient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IngredientResponse response = ingredientService.create(new IngredientRequest("  Flour ", Unit.G));

        assertThat(response.name()).isEqualTo("Flour");
        assertThat(response.defaultUnit()).isEqualTo(Unit.G);
    }

    @Test
    void rejectsDuplicatedIngredientName() {
        when(ingredientRepository.existsByNameIgnoreCase("Egg")).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.create(new IngredientRequest("Egg", Unit.PIECE)))
                .isInstanceOf(IngredientAlreadyExistsException.class);

        verify(ingredientRepository, never()).save(any());
    }

    @Test
    void searchesByNameWhenQueryIsProvided() {
        when(ingredientRepository.findByNameContainingIgnoreCaseOrderByNameAsc("tom"))
                .thenReturn(List.of(new Ingredient("Tomato", Unit.PIECE)));

        List<IngredientResponse> result = ingredientService.search(" tom ");

        assertThat(result).extracting(IngredientResponse::name).containsExactly("Tomato");
    }

    @Test
    void returnsAllIngredientsWhenQueryIsBlank() {
        when(ingredientRepository.findAll())
                .thenReturn(List.of(new Ingredient("Egg", Unit.PIECE), new Ingredient("Milk", Unit.ML)));

        List<IngredientResponse> result = ingredientService.search("  ");

        assertThat(result).hasSize(2);
    }
}
