package cz.rest.dto;

import java.util.List;

public record RecipeRequest(
        String name,
        String instructions,
        int servings,
        List<RecipeIngredientRequest> ingredients
) {}
