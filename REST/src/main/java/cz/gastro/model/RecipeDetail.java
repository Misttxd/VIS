package cz.gastro.model;

import java.util.List;

public record RecipeDetail(
        int id,
        String name,
        String instructions,
        int servings,
        List<RecipeIngredientDetail> ingredients
) {}
