package com.vis.rest;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<Ingredient> getAllItems() {
        return ingredientService.getAllItems();
    }

    @PostMapping
    public Long create(@RequestBody Ingredient ingredient) {
        return ingredientService.create(ingredient);
    }
}
