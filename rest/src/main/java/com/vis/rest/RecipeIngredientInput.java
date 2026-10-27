package com.vis.rest;

import java.math.BigDecimal;

public record RecipeIngredientInput(
        Long ingredientId,
        BigDecimal quantity,
        String unit,
        boolean optional
) {}
