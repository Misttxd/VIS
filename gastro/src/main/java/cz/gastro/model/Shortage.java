package cz.gastro.model;

import java.math.BigDecimal;

public record Shortage(
        long ingredientId,
        String name,
        String unit,
        BigDecimal missing
) {}
