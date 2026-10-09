package com.vis.rest;

import java.util.List;

public interface IngredientDao {
    List<Ingredient> getAllItems();

    Long create(Ingredient ingredient);
}
