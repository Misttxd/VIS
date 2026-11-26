package cz.gastro.controller;

import cz.gastro.model.Category;
import cz.gastro.model.Ingredient;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cz.gastro.service.CatalogService;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return catalogService.categories();
    }

    @PostMapping("/categories")
    public int addCategory(@RequestBody Category category) {
        return catalogService.addCategory(category.name());
    }

    @GetMapping("/ingredients")
    public List<Ingredient> ingredients(@RequestParam(defaultValue = "postgres") String storage) {
        return catalogService.ingredients(storage);
    }

    @PostMapping("/ingredients")
    public int addIngredient(@RequestParam(defaultValue = "postgres") String storage, @RequestBody Ingredient ingredient) {
        return catalogService.addIngredient(storage, ingredient);
    }
}
