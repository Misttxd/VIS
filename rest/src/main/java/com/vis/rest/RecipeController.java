package com.vis.rest;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/{id}")
    public List<RecipeDetail> getDetail(@PathVariable Long id) {
        return recipeService.getDetailById(id);
    }

    @PostMapping
    public Long create(@RequestBody RecipeCreateRequest request) {
        return recipeService.create(request);
    }
}
