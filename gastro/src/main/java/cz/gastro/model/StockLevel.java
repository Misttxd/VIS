package cz.gastro.model;

import java.math.BigDecimal;

public record StockLevel(
        long ingredientId,
        String name,
        String unit,
        BigDecimal available,
        boolean low
) {}
