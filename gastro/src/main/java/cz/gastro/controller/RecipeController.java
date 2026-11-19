package cz.gastro.controller;

import cz.gastro.model.Recipe;
import cz.gastro.model.RecipeLine;
import cz.gastro.model.RecipeDetail;
import cz.gastro.dto.RecipeRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cz.gastro.service.RecipeService;

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
    public long create(@RequestBody RecipeRequest request) {
        return recipeService.create(request);
    }

    @GetMapping("/recipes/{id}/ingredients")
    public List<RecipeLine> ingredients(@PathVariable long id) {
        return recipeService.ingredients(id);
    }

    @GetMapping("/recipes/{id}")
    public RecipeDetail detail(@PathVariable long id) {
        return recipeService.detail(id);
    }
}
