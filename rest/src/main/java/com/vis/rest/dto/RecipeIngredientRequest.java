package com.vis.rest.dto;

import java.math.BigDecimal;

public record RecipeIngredientRequest(
        Long ingredientId,
        String name,
        BigDecimal quantity,
        String unit,
        boolean optional
) {}
