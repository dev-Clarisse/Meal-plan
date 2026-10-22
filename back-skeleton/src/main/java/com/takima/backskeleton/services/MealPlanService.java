package com.takima.backskeleton.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.takima.backskeleton.DAO.MealDao;
import com.takima.backskeleton.DAO.MealPlanDao;
import com.takima.backskeleton.DTO.AddMealRequest;
import com.takima.backskeleton.DTO.MealDto;
import com.takima.backskeleton.DTO.MealPlanDto;
import com.takima.backskeleton.DTO.MealPlanMapper;
import com.takima.backskeleton.DTO.MissingIngredientDto;
import com.takima.backskeleton.DTO.MoveMealRequest;
import com.takima.backskeleton.models.Meal;
import com.takima.backskeleton.models.MealPlan;
import com.takima.backskeleton.recipe.RecipeRepository;

@Service
public class MealPlanService {
    private final MealPlanDao mealPlanDao;
    private final MealDao mealDao;
    private final MissingIngredientsService missingService;
    private final RecipeRepository recipeRepository;

    public MealPlanService(MealPlanDao mealPlanDao, MealDao mealDao,
                           MissingIngredientsService missingService, RecipeRepository recipeRepository) {
        this.mealPlanDao = mealPlanDao;
        this.mealDao = mealDao;
        this.missingService = missingService;
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public MealPlanDto getOrCreate(UUID userId, LocalDate anyDayOfWeek) {
        LocalDate monday = anyDayOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        MealPlan plan = mealPlanDao.findByUserIdAndWeekStart(userId, monday)
                .orElseGet(() -> mealPlanDao.save(new MealPlan(userId, monday)));
        return MealPlanMapper.toDto(plan);
    }

    @Transactional
    public MealDto addMeal(Long planId, UUID userId, AddMealRequest req) {
        MealPlan plan = findPlan(planId, userId);
        // Sinon la clé étrangère échoue en base et le client reçoit une erreur 500
        if (!recipeRepository.existsById(req.recipeId())) {
            throw new NoSuchElementException("Recipe not found");
        }
        Meal meal = new Meal(req.recipeId(), req.dayOfWeek(), req.mealType(), req.servings());
        plan.addMeal(meal);
        // On sauvegarde le repas lui-même : Hibernate remplit alors son id sur CET objet
        mealDao.saveAndFlush(meal);
        return MealPlanMapper.toDto(meal);
    }

    @Transactional
    public MealDto moveMeal(Long mealId, UUID userId, MoveMealRequest req) {
        Meal meal = mealDao.findByIdAndUserId(mealId, userId)
                .orElseThrow(() -> new NoSuchElementException("Meal not found"));
        meal.moveTo(req.dayOfWeek(), req.mealType());
        return MealPlanMapper.toDto(meal);
    }

    @Transactional(readOnly = true)
    public List<MissingIngredientDto> getMissingIngredients(Long planId, UUID userId) {
        MealPlan plan = findPlan(planId, userId);
        return missingService.compute(plan.getMeals(), userId);
    }

    private MealPlan findPlan(Long planId, UUID userId) {
        return mealPlanDao.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new NoSuchElementException("Meal plan not found"));
    }
}
