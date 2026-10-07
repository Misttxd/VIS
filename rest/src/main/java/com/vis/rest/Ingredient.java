package com.vis.rest;

public record Ingredient(
        Long id,
        Long categoryId,
        String name,
        String defaultUnit
) {}
