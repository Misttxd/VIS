package com.vis.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockBatch(
        Long id,
        Long ingredientId,
        BigDecimal quantity,
        LocalDate expirationDate,
        LocalDate receivedDate
) {}
