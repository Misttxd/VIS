package cz.gastro.model;

import java.math.BigDecimal;

public record RecipeDetailRow(
        long recipeId,
        String recipeName,
        String instructions,
        int servings,
        Long ingredientId,
        String ingredientName,
        String categoryName,
        String unit,
        BigDecimal quantity
) {}
