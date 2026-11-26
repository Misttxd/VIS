package cz.gastro.model;

import java.math.BigDecimal;

public record RecipeLine(
        int ingredientId,
        BigDecimal quantity
) {}
