package com.vis.rest;

import java.util.List;

public interface IngredientDao {
    List<Ingredient> findAll();

    Long create (Ingredient ingredient);
}
