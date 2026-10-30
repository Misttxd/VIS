package com.vis.rest.service;

import com.vis.rest.dao.IngredientDao;
import com.vis.rest.model.Ingredient;

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
