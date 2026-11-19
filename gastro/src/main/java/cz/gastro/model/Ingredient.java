package cz.gastro.model;

import java.math.BigDecimal;

public record Ingredient(
        Long id,
        String name,
        Long categoryId,
        String unit,
        BigDecimal minimum
) {}
