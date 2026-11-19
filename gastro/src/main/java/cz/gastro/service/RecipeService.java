package cz.gastro.service;

import cz.gastro.dao.PostgresIngredientDao;
import cz.gastro.dto.RecipeIngredientRequest;
import cz.gastro.model.Ingredient;
import java.math.BigDecimal;
import cz.gastro.model.Recipe;
import cz.gastro.model.RecipeLine;
import cz.gastro.model.RecipeDetail;
import cz.gastro.model.RecipeIngredientDetail;
import cz.gastro.dao.RecipeDao;
import cz.gastro.dao.RecipeIngredientDao;
import cz.gastro.dto.RecipeRequest;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {
    private final RecipeDao recipeDao;
    private final RecipeIngredientDao recipeIngredientDao;

    private final PostgresIngredientDao ingredientDao;

    public RecipeService(RecipeDao recipeDao,
                         RecipeIngredientDao recipeIngredientDao,
                         PostgresIngredientDao ingredientDao) {
        this.ingredientDao = ingredientDao;
        this.recipeDao = recipeDao;
        this.recipeIngredientDao = recipeIngredientDao;
    }

    @Transactional
    public long create(RecipeRequest request) {
        if (request.ingredients() == null || request.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Přidejte alespoň jednu surovinu.");
        }
        Recipe recipe = new Recipe(null, request.name(), request.instructions(), request.servings());
        long recipeId = recipeDao.create(recipe);
        for (RecipeIngredientRequest item : request.ingredients()) {
            Long ingredientId = item.ingredientId();
            if (ingredientId == null) {
                if (item.name() == null || item.name().isBlank() || item.categoryId() == null || item.unit() == null) {
                    throw new IllegalArgumentException("Nová surovina potřebuje název, kategorii a jednotku.");
                }
                Ingredient ingredient = new Ingredient(null, item.name(), item.categoryId(), item.unit(), BigDecimal.ZERO);
                ingredientId = ingredientDao.create(ingredient);
            }
            recipeIngredientDao.create(recipeId, new RecipeLine(ingredientId, item.quantity()));
        }
        return recipeId;
    }

    public List<Recipe> recipes() {
        return recipeDao.getAllItems();
    }

    public List<RecipeLine> ingredients(long recipeId) {
        return recipeIngredientDao.forRecipe(recipeId);
    }

    public RecipeDetail detail(long recipeId) {
        var rows = recipeDao.findDetail(recipeId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Recept neexistuje.");
        }
        var recipe = rows.getFirst();
        List<RecipeIngredientDetail> ingredients = new ArrayList<>();
        for (var row : rows) {
            if (row.ingredientId() != null) {
                ingredients.add(new RecipeIngredientDetail(row.ingredientId(), row.ingredientName(),
                        row.categoryName(), row.unit(), row.quantity()));
            }
        }
        return new RecipeDetail(recipe.recipeId(), recipe.recipeName(), recipe.instructions(),
                recipe.servings(), ingredients);
    }
}
