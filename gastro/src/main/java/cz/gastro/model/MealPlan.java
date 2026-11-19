package cz.gastro.model;

import java.time.LocalDate;

public record MealPlan(
        long id,
        LocalDate day,
        boolean prepared
) {}
