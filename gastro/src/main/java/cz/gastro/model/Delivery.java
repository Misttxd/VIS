package cz.gastro.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Delivery(
        Long id,
        Long ingredientId,
        BigDecimal remaining,
        LocalDate expires
) {}
