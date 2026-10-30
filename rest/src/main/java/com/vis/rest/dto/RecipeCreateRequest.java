package com.vis.rest.dto;

import java.util.List;

public record RecipeCreateRequest(
        String name,
        String instructions,
        Integer preparationTimeMinutes,
        Integer servings,
        List<RecipeIngredientRequest> ingredients
) {}
