package com.takima.backskeleton.integration;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.takima.backskeleton.DTO.AddMealRequest;
import com.takima.backskeleton.DTO.MealPlanDto;
import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.DTO.ShoppingListDto;
import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.Inventory.models.Unit;
import com.takima.backskeleton.User.DAO.UserRepository;
import com.takima.backskeleton.User.models.User;
import com.takima.backskeleton.models.MealType;
import com.takima.backskeleton.recipe.RecipeRepository;
import com.takima.backskeleton.services.MealPlanService;
import com.takima.backskeleton.services.ShoppingListService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EggScenarioIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired InventoryRepository inventoryRepository;
    @Autowired RecipeRepository recipeRepository;
    @Autowired MealPlanService mealPlanService;
    @Autowired ShoppingListService shoppingListService;

    private Long recipeId(String title) {
        return recipeRepository.search(title, PageRequest.of(0, 1)).getContent().get(0).getId();
    }

    private void stock(User user, String name, String qty, Unit unit, LocalDate expiry) {
        inventoryRepository.save(new Inventory(user, name, new BigDecimal(qty), unit, expiry));
    }

    @Test
    void eggsScenario_8_needed_6_in_stock_2_missing_and_expired_stock_ignored() {
        User user = userRepository.save(
                new User("eggs-" + UUID.randomUUID() + "@test.com", "hash", java.time.Instant.now()));
        UUID userId = user.getId();

        // Stock : 6 oeufs valides, 10 oeufs périmés (ignorés), beurre et lait suffisants
        LocalDate future = LocalDate.now().plusDays(60);
        stock(user, "Egg", "6", Unit.PIECE, future);
        stock(user, "Egg", "10", Unit.PIECE, LocalDate.now().minusDays(30));
        stock(user, "Butter", "200", Unit.GRAM, future);
        stock(user, "Milk", "1", Unit.LITER, future);

        // Plan : Omelette (2 portions = 4 oeufs) + Cake (6 portions = 4 oeufs) = 8 oeufs
        MealPlanDto plan = mealPlanService.getOrCreate(userId, LocalDate.of(2026, 10, 26));
        mealPlanService.addMeal(plan.id(), userId,
                new AddMealRequest(recipeId("Omelette"), DayOfWeek.MONDAY, MealType.LUNCH, 2));
        mealPlanService.addMeal(plan.id(), userId,
                new AddMealRequest(recipeId("Cake"), DayOfWeek.TUESDAY, MealType.DINNER, 6));

        List<MissingIngredientDto> missing = mealPlanService.getMissingIngredients(plan.id(), userId);

        assertEquals(1, missing.size());
        assertEquals("Egg", missing.get(0).name());
        assertEquals(0, new BigDecimal("2").compareTo(missing.get(0).quantity()));

        // Liste de courses : 2 oeufs, et pas de doublon si on régénère
        ShoppingListDto first = shoppingListService.generate(plan.id(), userId);
        ShoppingListDto second = shoppingListService.generate(plan.id(), userId);
        assertEquals(1, first.items().size());
        assertEquals(1, second.items().size());
        assertEquals(0, new BigDecimal("2").compareTo(second.items().get(0).quantity()));
    }

    @Test
    void enoughStock_gives_empty_shopping_list() {
        User user = userRepository.save(
                new User("full-" + UUID.randomUUID() + "@test.com", "hash", java.time.Instant.now()));
        UUID userId = user.getId();
        LocalDate future = LocalDate.now().plusDays(60);
        stock(user, "Egg", "20", Unit.PIECE, future);
        stock(user, "Butter", "500", Unit.GRAM, future);
        stock(user, "Milk", "2", Unit.LITER, future);

        MealPlanDto plan = mealPlanService.getOrCreate(userId, LocalDate.of(2026, 11, 2));
        mealPlanService.addMeal(plan.id(), userId,
                new AddMealRequest(recipeId("Omelette"), DayOfWeek.MONDAY, MealType.LUNCH, 2));
        mealPlanService.addMeal(plan.id(), userId,
                new AddMealRequest(recipeId("Cake"), DayOfWeek.TUESDAY, MealType.DINNER, 6));

        ShoppingListDto list = shoppingListService.generate(plan.id(), userId);
        assertTrue(list.empty());
    }

    @Test
    void addMeal_with_unknown_recipe_is_rejected() {
        User user = userRepository.save(
                new User("unknown-" + UUID.randomUUID() + "@test.com", "hash", java.time.Instant.now()));
        MealPlanDto plan = mealPlanService.getOrCreate(user.getId(), LocalDate.of(2026, 11, 9));

        assertThrows(NoSuchElementException.class, () -> mealPlanService.addMeal(plan.id(), user.getId(),
                new AddMealRequest(999_999L, DayOfWeek.MONDAY, MealType.LUNCH, 2)));
    }
}