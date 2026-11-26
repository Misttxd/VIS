package cz.gastro.model;

import java.math.BigDecimal;

public record StockLevel(
        int ingredientId,
        String name,
        String unit,
        BigDecimal available,
        boolean low
) {}
