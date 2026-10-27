package com.vis.rest;

import java.util.List;

public record RecipeCreateRequest(
        String name,
        String instructions,
        Integer preparationTimeMinutes,
        Integer servings,
        List<RecipeIngredientInput> ingredients
) {}
