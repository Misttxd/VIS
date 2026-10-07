package com.vis.rest;

public record Recipe(
        Long id,
        String name,
        String instructions,
        Integer preparationTimeMinutes,
        Integer servings
) {}
