package com.vis.rest;

import java.math.BigDecimal;

public record RecipeDetail(
        Long recipeId,
        String recipeName,
        String instructions,
        Integer preparationTimeMinutes,
        Integer servings,
        Long ingredientId,
        String ingredientName,
        BigDecimal quantity,
        String unit,
        boolean optional
) {}