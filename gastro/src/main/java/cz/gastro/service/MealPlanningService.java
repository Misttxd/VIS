package cz.gastro.service;

import cz.gastro.model.Availability;
import cz.gastro.model.Delivery;
import cz.gastro.model.MealPlan;
import cz.gastro.model.PlanLine;
import cz.gastro.model.Recipe;
import cz.gastro.model.RecipeLine;
import cz.gastro.model.Shortage;
import cz.gastro.model.StockLevel;
import cz.gastro.dao.MealPlanDao;
import cz.gastro.dao.MealPlanItemDao;
import cz.gastro.dao.DeliveryDao;
import cz.gastro.dao.RecipeDao;
import cz.gastro.dao.RecipeIngredientDao;
import cz.gastro.dto.PlanRequest;
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

@Service
public class MealPlanningService {
    private final StockService stockService;
    private final RecipeDao recipeDao;
    private final RecipeIngredientDao recipeIngredientDao;
    private final MealPlanDao mealPlanDao;
    private final MealPlanItemDao mealPlanItemDao;
    private final DeliveryDao deliveryDao;

    public MealPlanningService(StockService stockService,
                               RecipeDao recipeDao,
                               RecipeIngredientDao recipeIngredientDao,
                               MealPlanDao mealPlanDao,
                               MealPlanItemDao mealPlanItemDao,
                               DeliveryDao deliveryDao) {
        this.stockService = stockService;
        this.recipeDao = recipeDao;
        this.recipeIngredientDao = recipeIngredientDao;
        this.mealPlanDao = mealPlanDao;
        this.mealPlanItemDao = mealPlanItemDao;
        this.deliveryDao = deliveryDao;
    }

    public List<Availability> availability(LocalDate day) {
        Map<Long, BigDecimal> available = new HashMap<>();
        for (StockLevel stock : stockService.levels(day)) {
            available.put(stock.ingredientId(), stock.available());
        }
        Set<Long> expiring = new HashSet<>();
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
    public long save(PlanRequest request) {
        if (request.day() == null) {
            throw new IllegalArgumentException("Vyber datum plánu.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Plán musí obsahovat alespoň jedno jídlo.");
        }
        Set<Long> recipesInPlan = new HashSet<>();
        for (PlanLine item : request.items()) {
            if (item.portions() < 1) {
                throw new IllegalArgumentException("Počet porcí musí být alespoň 1.");
            }
            if (!recipesInPlan.add(item.recipeId())) {
                throw new IllegalArgumentException("Stejné jídlo je v plánu dvakrát. Zadej jeho porce dohromady.");
            }
        }
        long planId = mealPlanDao.create(request.day());
        for (PlanLine item : request.items()) {
            mealPlanItemDao.create(planId, item);
        }
        return planId;
    }

    public List<MealPlan> plans() {
        return mealPlanDao.getAllItems();
    }

    public List<PlanLine> items(long planId) {
        return mealPlanItemDao.forPlan(planId);
    }

    public List<Shortage> shoppingList(long planId) {
        MealPlan plan = mealPlanDao.getById(planId);
        if (plan.prepared()) {
            return List.of();
        }
        Map<Long, BigDecimal> required = requiredQuantities(planId);
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
    public void prepare(long planId) {
        MealPlan plan = mealPlanDao.lockForPreparation(planId);
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

    private Map<Long, BigDecimal> requiredQuantities(long planId) {
        Map<Long, Recipe> recipes = new HashMap<>();
        for (Recipe recipe : recipeDao.getAllItems()) {
            recipes.put(recipe.id(), recipe);
        }
        // Stejné pořadí surovin i při potvrzování různých plánů současně.
        Map<Long, BigDecimal> required = new TreeMap<>();
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
