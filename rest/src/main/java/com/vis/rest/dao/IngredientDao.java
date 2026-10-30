package com.vis.rest.dao;

import com.vis.rest.model.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientDao {
    List<Ingredient> getAllItems();

    Long create(Ingredient ingredient);

    Optional<Ingredient> findByName(String name);
}
