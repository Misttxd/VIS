package cz.gastro.dto;

import java.math.BigDecimal;

public record RecipeIngredientRequest(
        Integer ingredientId,
        String name,
        Integer categoryId,
        String unit,
        BigDecimal quantity
) {}
