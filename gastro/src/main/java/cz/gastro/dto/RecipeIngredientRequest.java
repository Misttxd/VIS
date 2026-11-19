package cz.gastro.dto;

import java.math.BigDecimal;

public record RecipeIngredientRequest(
        Long ingredientId,
        String name,
        Long categoryId,
        String unit,
        BigDecimal quantity
) {}
