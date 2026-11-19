package cz.gastro.model;

public record Recipe(
        Long id,
        String name,
        String instructions,
        int servings
) {}
