package com.vis.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {

    private final IngredientDao ingredientDao;

    public IngredientService(
            IngredientDAOFactory factory,
            @Value("${storage.type}") String storageType
    ) {
        this.ingredientDao = factory.create(storageType);
    }

    public List<Ingredient> findAll() {
        return ingredientDao.findAll();
    }

    public Long create(Ingredient ingredient) {
        return ingredientDao.create(ingredient);
    }
}