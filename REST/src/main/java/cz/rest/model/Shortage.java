package cz.rest.model;

import java.math.BigDecimal;

public record Shortage(
        int ingredientId,
        String name,
        String unit,
        BigDecimal missing
) {}
