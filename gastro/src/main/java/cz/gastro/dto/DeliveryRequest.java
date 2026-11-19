package cz.gastro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliveryRequest(
        Long ingredientId,
        String name,
        Long categoryId,
        String unit,
        BigDecimal remaining,
        LocalDate expires
) {}
