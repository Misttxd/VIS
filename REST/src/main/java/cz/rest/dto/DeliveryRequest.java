package cz.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliveryRequest(
        Integer ingredientId,
        String name,
        Integer categoryId,
        String unit,
        BigDecimal remaining,
        LocalDate expires
) {}
