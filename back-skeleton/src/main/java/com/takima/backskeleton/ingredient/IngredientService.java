package com.takima.backskeleton.ingredient;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> search(String query) {
        List<Ingredient> ingredients = (query == null || query.isBlank())
                ? ingredientRepository.findAll()
                : ingredientRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        return ingredients.stream().map(IngredientResponse::from).toList();
    }

    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        String name = request.name().trim();
        if (ingredientRepository.existsByNameIgnoreCase(name)) {
            throw new IngredientAlreadyExistsException(name);
        }
        Ingredient saved = ingredientRepository.save(new Ingredient(name, request.defaultUnit()));
        return IngredientResponse.from(saved);
    }
}
