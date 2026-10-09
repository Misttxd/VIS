package com.vis.rest;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {

    private final IngredientDao ingredientDao;

    public IngredientService(IngredientDao ingredientDao) {
        this.ingredientDao = ingredientDao;
    }

    public List<Ingredient> getAllItems() {
        return ingredientDao.getAllItems();
    }

    public Long create(Ingredient ingredient) {
        return ingredientDao.create(ingredient);
    }
}
