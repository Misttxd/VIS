package cz.gastro.model;

public record Availability(
        int recipeId,
        String name,
        long portions,
        boolean useSoon
) {}
