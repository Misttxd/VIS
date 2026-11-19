package cz.gastro.model;

import java.math.BigDecimal;

public record RecipeLine(
        long ingredientId,
        BigDecimal quantity
) {}
