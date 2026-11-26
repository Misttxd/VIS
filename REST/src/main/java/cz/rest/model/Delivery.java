package cz.rest.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Delivery(
        Integer id,
        Integer ingredientId,
        BigDecimal remaining,
        LocalDate expires
) {}
