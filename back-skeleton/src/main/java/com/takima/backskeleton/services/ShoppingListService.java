package com.takima.backskeleton.services;
import java.util.UUID;

import com.takima.backskeleton.DAO.MealPlanDao;
import com.takima.backskeleton.DAO.ShoppingItemDao;
import com.takima.backskeleton.DAO.ShoppingListDao;
import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.DTO.ShoppingItemDto;
import com.takima.backskeleton.DTO.ShoppingListDto;
import com.takima.backskeleton.DTO.ShoppingListMapper;
import com.takima.backskeleton.models.MealPlan;
import com.takima.backskeleton.models.ShoppingItem;
import com.takima.backskeleton.models.ShoppingList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShoppingListService {
    private final MealPlanDao mealPlanDao;
    private final ShoppingListDao shoppingListDao;
    private final ShoppingItemDao shoppingItemDao;
    private final MissingIngredientsService missingService;

    public ShoppingListService(MealPlanDao mealPlanDao, ShoppingListDao shoppingListDao,
                               ShoppingItemDao shoppingItemDao, MissingIngredientsService missingService) {
        this.mealPlanDao = mealPlanDao;
        this.shoppingListDao = shoppingListDao;
        this.shoppingItemDao = shoppingItemDao;
        this.missingService = missingService;
    }

    @Transactional
    public ShoppingListDto generate(Long planId, UUID userId) {
        MealPlan plan = mealPlanDao.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new NoSuchElementException("Meal plan not found"));
        List<MissingIngredientDto> missing = missingService.compute(plan.getMeals(), userId);

        ShoppingList list = shoppingListDao.findByMealPlanId(planId)
                .orElseGet(() -> new ShoppingList(plan));
        synchronize(list, missing);
        list.markGeneratedNow();

        ShoppingList saved = shoppingListDao.saveAndFlush(list);
        return ShoppingListMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ShoppingListDto get(Long planId, UUID userId) {
        mealPlanDao.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new NoSuchElementException("Meal plan not found"));
        ShoppingList list = shoppingListDao.findByMealPlanId(planId)
                .orElseThrow(() -> new NoSuchElementException("Shopping list not generated yet"));
        return ShoppingListMapper.toDto(list);
    }

    @Transactional
    public ShoppingItemDto setPurchased(Long itemId, UUID userId, boolean purchased) {
        ShoppingItem item = shoppingItemDao.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NoSuchElementException("Shopping item not found"));
        item.setPurchased(purchased);
        return ShoppingListMapper.toDto(item);
    }

    /**
     * Met la liste à jour SANS doublon et en conservant les éléments cochés :
     * - un ingrédient déjà dans la liste est mis à jour (sa case cochée est conservée) ;
     * - un nouvel ingrédient manquant est ajouté ;
     * - un élément non coché qui ne manque plus est retiré ;
     * - un élément déjà coché est toujours conservé.
     */
    static void synchronize(ShoppingList list, List<MissingIngredientDto> missing) {
        Set<Long> missingIds = missing.stream()
                .map(MissingIngredientDto::ingredientId)
                .collect(Collectors.toSet());

        list.getItems().removeIf(i -> !i.isPurchased() && !missingIds.contains(i.getIngredientId()));

        Map<Long, ShoppingItem> existing = new HashMap<>();
        list.getItems().forEach(i -> existing.put(i.getIngredientId(), i));

        for (MissingIngredientDto m : missing) {
            ShoppingItem item = existing.get(m.ingredientId());
            if (item != null) {
                item.update(m.name(), m.quantity(), m.unit());
            } else {
                list.addItem(new ShoppingItem(m.ingredientId(), m.name(), m.quantity(), m.unit()));
            }
        }
    }
}
