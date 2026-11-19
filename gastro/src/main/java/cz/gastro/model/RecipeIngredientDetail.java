package cz.gastro.model;

import java.math.BigDecimal;

public record RecipeIngredientDetail(
        long ingredientId,
        String name,
        String category,
        String unit,
        BigDecimal quantity
) {}
