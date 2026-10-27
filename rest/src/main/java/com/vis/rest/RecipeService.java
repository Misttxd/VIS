package com.vis.rest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
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
        Long recipeId = recipeRepository.create(recipe);

        for (RecipeIngredientInput ingredient : request.ingredients()) {
            recipeIngredientRepository.create(new RecipeIngredient(
                    recipeId,
                    ingredient.ingredientId(),
                    ingredient.quantity(),
                    ingredient.unit(),
                    ingredient.optional()
            ));
        }

        return recipeId;
    }
}
