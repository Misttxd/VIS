package cz.gastro.model;

public record Availability(
        long recipeId,
        String name,
        long portions,
        boolean useSoon
) {}
