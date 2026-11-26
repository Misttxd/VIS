package cz.gastro.model;

public record Recipe(
        Integer id,
        String name,
        String instructions,
        int servings
) {}
