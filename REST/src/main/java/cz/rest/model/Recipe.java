package cz.rest.model;

public record Recipe(
        Integer id,
        String name,
        String instructions,
        int servings
) {}
