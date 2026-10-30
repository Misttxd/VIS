package com.vis.rest.model;

public record Ingredient(
        Long id,
        Long categoryId,
        String name,
        String defaultUnit
) {}
