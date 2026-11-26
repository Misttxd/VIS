package cz.rest.model;

import java.math.BigDecimal;

public record Ingredient(
        Integer id,
        String name,
        Integer categoryId,
        String unit,
        BigDecimal minimum
) {}
