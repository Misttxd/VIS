package cz.rest.controller;

import cz.rest.model.Recipe;
import cz.rest.model.RecipeLine;
import cz.rest.model.RecipeDetail;
import cz.rest.dto.RecipeRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cz.rest.service.RecipeService;

@RestController
@RequestMapping("/api")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/recipes")
    public List<Recipe> recipes() {
        return recipeService.recipes();
    }

    @PostMapping("/recipes")
    public int create(@RequestBody RecipeRequest request) {
        return recipeService.create(request);
    }

    @GetMapping("/recipes/{id}/ingredients")
    public List<RecipeLine> ingredients(@PathVariable int id) {
        return recipeService.ingredients(id);
    }

    @GetMapping("/recipes/{id}")
    public RecipeDetail detail(@PathVariable int id) {
        return recipeService.detail(id);
    }
}
