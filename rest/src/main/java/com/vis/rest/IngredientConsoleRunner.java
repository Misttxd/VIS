package com.vis.rest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class IngredientConsoleRunner implements CommandLineRunner {

    private final IngredientService ingredientService;

    public IngredientConsoleRunner(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Override
    public void run(String... args) {
        System.out.println("Ingredients:");
        ingredientService.getAllItems().forEach(System.out::println);
    }
}
