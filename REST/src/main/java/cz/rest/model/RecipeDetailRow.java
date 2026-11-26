package cz.rest.model;

import java.math.BigDecimal;

public record RecipeDetailRow(
        int recipeId,
        String recipeName,
        String instructions,
        int servings,
        Integer ingredientId,
        String ingredientName,
        String categoryName,
        String unit,
        BigDecimal quantity
) {}
