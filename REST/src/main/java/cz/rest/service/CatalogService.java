package cz.rest.service;

import cz.rest.model.Category;
import cz.rest.model.Ingredient;
import cz.rest.dao.CategoryDao;
import java.util.List;
import org.springframework.stereotype.Service;
import cz.rest.factory.IngredientDaoFactory;

@Service
public class CatalogService {
    private final CategoryDao categoryDao;
    private final IngredientDaoFactory ingredientDaoFactory;

    public CatalogService(CategoryDao categoryDao,
                          IngredientDaoFactory ingredientDaoFactory) {
        this.categoryDao = categoryDao;
        this.ingredientDaoFactory = ingredientDaoFactory;
    }

    public List<Category> categories() {
        return categoryDao.getAllItems();
    }

    public int addCategory(String name) {
        return categoryDao.create(name);
    }

    public List<Ingredient> ingredients(String storage) {
        return ingredientDaoFactory.select(storage).getAllItems();
    }

    public int addIngredient(String storage, Ingredient ingredient) {
        if (ingredient.name() == null || ingredient.name().isBlank() || ingredient.categoryId() == null) {
            throw new IllegalArgumentException("Vyplňte jméno a kategorii suroviny.");
        }
        if (ingredient.minimum() == null || ingredient.minimum().signum() < 0
                || !List.of("g", "ml", "ks").contains(ingredient.unit())) {
            throw new IllegalArgumentException("Zkontrolujte jednotku a minimální zásobu.");
        }
        return ingredientDaoFactory.select(storage).create(ingredient);
    }
}
