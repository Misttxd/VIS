package cz.rest.model;

import java.time.LocalDate;

public record MealPlan(
        int id,
        LocalDate day,
        boolean prepared,
        int userId
) {}
