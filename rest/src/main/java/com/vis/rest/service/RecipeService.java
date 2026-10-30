package com.vis.rest.service;

import com.vis.rest.dao.IngredientDao;
import com.vis.rest.dao.PostgresRecipeDao;
import com.vis.rest.dao.PostgresRecipeIngredientDao;
import com.vis.rest.factory.IngredientDaoFactory;
import com.vis.rest.model.Ingredient;
import com.vis.rest.repository.RecipeRepository;
import com.vis.rest.dto.RecipeCreateRequest;
import com.vis.rest.dto.RecipeIngredientRequest;
import com.vis.rest.model.Recipe;
import com.vis.rest.model.RecipeDetail;
import com.vis.rest.model.RecipeIngredient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final PostgresRecipeDao recipeDao;
    private final PostgresRecipeIngredientDao recipeIngredientDao;
    private final IngredientDao ingredientDao;

    public RecipeService(RecipeRepository recipeRepository,
                         PostgresRecipeDao recipeDao,
                         PostgresRecipeIngredientDao recipeIngredientDao,
                         IngredientDaoFactory ingredientDaoFactory) {
        this.recipeRepository = recipeRepository;
        this.recipeDao = recipeDao;
        this.recipeIngredientDao = recipeIngredientDao;
        this.ingredientDao = ingredientDaoFactory.create("postgres");
    }

    public List<RecipeDetail> getDetailById(Long recipeId) {
        return recipeRepository.findDetailById(recipeId);
    }

    @Transactional
    public Long create(RecipeCreateRequest request) {
        Recipe recipe = new Recipe(
                null,
                request.name(),
                request.instructions(),
                request.preparationTimeMinutes(),
                request.servings()
        );
        Long recipeId = recipeDao.create(recipe);

        for (RecipeIngredientRequest ingredient : request.ingredients()) {
            Long ingredientId = ingredient.ingredientId();

            if (ingredientId == null) {
                var existingIngredient = ingredientDao.findByName(ingredient.name());

                if (existingIngredient.isPresent()) {
                    ingredientId = existingIngredient.get().id();
                } else {
                    ingredientId = ingredientDao.create(new Ingredient(
                            null, null, ingredient.name().trim(), ingredient.unit()
                    ));
                }
            }

            recipeIngredientDao.create(new RecipeIngredient(
                    recipeId,
                    ingredientId,
                    ingredient.quantity(),
                    ingredient.unit(),
                    ingredient.optional()
            ));
        }

        return recipeId;
    }

}
