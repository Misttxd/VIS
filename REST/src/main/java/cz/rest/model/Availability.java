package cz.rest.model;

public record Availability(
        int recipeId,
        String name,
        long portions,
        boolean useSoon
) {}
