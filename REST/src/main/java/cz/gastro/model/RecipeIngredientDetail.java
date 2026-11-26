package cz.gastro.model;

import java.math.BigDecimal;

public record RecipeIngredientDetail(
        int ingredientId,
        String name,
        String category,
        String unit,
        BigDecimal quantity
) {}
