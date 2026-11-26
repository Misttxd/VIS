package cz.rest.service;

import cz.rest.model.Availability;
import cz.rest.model.Delivery;
import cz.rest.model.MealPlan;
import cz.rest.model.PlanLine;
import cz.rest.model.Recipe;
import cz.rest.model.RecipeLine;
import cz.rest.model.Shortage;
import cz.rest.model.StockLevel;
import cz.rest.dao.MealPlanDao;
import cz.rest.dao.MealPlanItemDao;
import cz.rest.dao.DeliveryDao;
import cz.rest.dao.RecipeDao;
import cz.rest.dao.RecipeIngredientDao;
import cz.rest.dto.PlanRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cz.rest.auth.CurrentUser;

import cz.rest.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MealPlanningService {
    private final StockService stockService;
    private final RecipeDao recipeDao;
    private final RecipeIngredientDao recipeIngredientDao;
    private final MealPlanDao mealPlanDao;
    private final MealPlanItemDao mealPlanItemDao;
    private final DeliveryDao deliveryDao;

    private final CurrentUser currentUser;

    public MealPlanningService(StockService stockService, RecipeDao recipeDao, RecipeIngredientDao recipeIngredientDao, MealPlanDao mealPlanDao, MealPlanItemDao mealPlanItemDao, DeliveryDao deliveryDao, CurrentUser currentUser) {
        this.stockService = stockService;
        this.recipeDao = recipeDao;
        this.recipeIngredientDao = recipeIngredientDao;
        this.mealPlanDao = mealPlanDao;
        this.mealPlanItemDao = mealPlanItemDao;
        this.deliveryDao = deliveryDao;
        this.currentUser = currentUser;
    }

    public List<Availability> availability(LocalDate day) {
        Map<Integer, BigDecimal> available = new HashMap<>();
        for (StockLevel stock : stockService.levels(day)) {
            available.put(stock.ingredientId(), stock.available());
        }
        Set<Integer> expiring = new HashSet<>();
        for (Delivery delivery : stockService.expiring(day)) {
            expiring.add(delivery.ingredientId());
        }

        List<Availability> result = new ArrayList<>();
        for (Recipe recipe : recipeDao.getAllItems()) {
            long portions = Long.MAX_VALUE;
            boolean useSoon = false;
            for (RecipeLine line : recipeIngredientDao.forRecipe(recipe.id())) {
                BigDecimal stock = available.get(line.ingredientId());
                BigDecimal possible = stock.multiply(BigDecimal.valueOf(recipe.servings()))
                        .divide(line.quantity(), 0, RoundingMode.DOWN);
                portions = Math.min(portions, possible.longValueExact());
                if (expiring.contains(line.ingredientId())) {
                    useSoon = true;
                }
            }
            if (portions == 0) {
                useSoon = false;
            }
            result.add(new Availability(recipe.id(), recipe.name(), portions, useSoon));
        }
        result.sort(Comparator.comparing(Availability::useSoon).reversed());
        return result;
    }

    @Transactional
    public int save(PlanRequest request) {
        int userId = currentUser.requireUser().id();

        if (request.day() == null) {
            throw new IllegalArgumentException("Vyber datum plánu.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Plán musí obsahovat alespoň jedno jídlo.");
        }
        Set<Integer> recipesInPlan = new HashSet<>();
        for (PlanLine item : request.items()) {
            if (item.portions() < 1) {
                throw new IllegalArgumentException("Počet porcí musí být alespoň 1.");
            }
            if (!recipesInPlan.add(item.recipeId())) {
                throw new IllegalArgumentException("Stejné jídlo je v plánu dvakrát. Zadej jeho porce dohromady.");
            }
        }
        int planId = mealPlanDao.create(request.day(), userId);
        for (PlanLine item : request.items()) {
            mealPlanItemDao.create(planId, item);
        }
        return planId;
    }

    public List<MealPlan> plans() {
        User user = currentUser.requireUser();
        List<MealPlan> plans = mealPlanDao.getAllItems();

        if (user.isAdmin()) {
            return plans;
        }

        List<MealPlan> ownPlans = new ArrayList<>();
        for (MealPlan plan : plans) {
            if (plan.userId() == user.id()) {
                ownPlans.add(plan);
            }
        }
        return ownPlans;
    }

    private void checkAccess(MealPlan plan) {
        User user = currentUser.requireUser();

        if (!user.isAdmin() && plan.userId() != user.id()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "K tomuto plánu nemáš přístup."
            );
        }
    }

    public List<PlanLine> items(int planId) {
        checkAccess(mealPlanDao.getById(planId));
        return mealPlanItemDao.forPlan(planId);
    }

    public List<Shortage> shoppingList(int planId) {
        MealPlan plan = mealPlanDao.getById(planId);
        checkAccess(plan);
        if (plan.prepared()) {
            return List.of();
        }
        Map<Integer, BigDecimal> required = requiredQuantities(planId);
        List<Shortage> missing = new ArrayList<>();
        for (StockLevel stock : stockService.levels(plan.day())) {
            BigDecimal needed = required.getOrDefault(stock.ingredientId(), BigDecimal.ZERO);
            BigDecimal difference = needed.subtract(stock.available());
            if (difference.signum() > 0) {
                missing.add(new Shortage(stock.ingredientId(), stock.name(), stock.unit(), difference));
            }
        }
        return missing;
    }

    @Transactional
    public void prepare(int planId) {
        MealPlan plan = mealPlanDao.lockForPreparation(planId);
        checkAccess(plan);
        if (plan.prepared()) {
            throw new IllegalArgumentException("Tento plán už byl připraven a zásoby odepsány.");
        }
        LocalDate preparationDay = LocalDate.now();
        for (var item : requiredQuantities(planId).entrySet()) {
            BigDecimal remaining = item.getValue();
            for (Delivery delivery : deliveryDao.lockUsable(item.getKey(), preparationDay)) {
                BigDecimal quantity = remaining.min(delivery.remaining());
                if (deliveryDao.withdraw(delivery.id(), quantity) == 0) {
                    throw new IllegalArgumentException("Zásoba se změnila. Zkuste potvrzení znovu.");
                }
                remaining = remaining.subtract(quantity);
                if (remaining.signum() == 0) {
                    break;
                }
            }
            if (remaining.signum() > 0) {
                throw new IllegalArgumentException("Na přípravu plánu není dostatek surovin. Nic nebylo odepsáno.");
            }
        }
        mealPlanDao.markPrepared(planId);
    }

    private Map<Integer, BigDecimal> requiredQuantities(int planId) {
        Map<Integer, Recipe> recipes = new HashMap<>();
        for (Recipe recipe : recipeDao.getAllItems()) {
            recipes.put(recipe.id(), recipe);
        }
        // Stejné pořadí surovin i při potvrzování různých plánů současně.
        Map<Integer, BigDecimal> required = new TreeMap<>();
        for (PlanLine item : items(planId)) {
            Recipe recipe = recipes.get(item.recipeId());
            for (RecipeLine line : recipeIngredientDao.forRecipe(recipe.id())) {
                BigDecimal quantity = line.quantity().multiply(BigDecimal.valueOf(item.portions()))
                        .divide(BigDecimal.valueOf(recipe.servings()), MathContext.DECIMAL128);
                required.merge(line.ingredientId(), quantity, BigDecimal::add);
            }
        }

        for (var item : required.entrySet()) {
            item.setValue(item.getValue().setScale(6, RoundingMode.HALF_UP));
        }
        return required;
    }
}
