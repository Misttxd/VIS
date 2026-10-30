package com.vis.rest.dao;

import com.vis.rest.model.RecipeIngredient;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresRecipeIngredientDao {

    private final JdbcClient jdbcClient;

    public PostgresRecipeIngredientDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void create(RecipeIngredient recipeIngredient) {
        jdbcClient.sql("""
                INSERT INTO recipe_ingredient
                    (recipe_id, ingredient_id, quantity, unit, optional)
                VALUES
                    (:recipeId, :ingredientId, :quantity, :unit, :optional)
                """)
                .param("recipeId", recipeIngredient.recipeId())
                .param("ingredientId", recipeIngredient.ingredientId())
                .param("quantity", recipeIngredient.quantity())
                .param("unit", recipeIngredient.unit())
                .param("optional", recipeIngredient.optional())
                .update();
    }
}
