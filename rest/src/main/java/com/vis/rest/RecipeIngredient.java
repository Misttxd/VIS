package com.vis.rest;

import java.math.BigDecimal;

public record RecipeIngredient(
        Long recipeId,
        Long ingredientId,
        BigDecimal quantity,
        String unit,
        boolean optional
) {}
