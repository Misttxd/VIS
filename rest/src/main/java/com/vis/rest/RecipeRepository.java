package com.vis.rest;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RecipeRepository {

    private final JdbcClient jdbcClient;

    public RecipeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Recipe> findAll() {
        return jdbcClient.sql("""
                SELECT id, name, instructions, preparation_time_minutes, servings
                FROM recipe
                ORDER BY name
                """)
                .query(Recipe.class)
                .list();
    }

    public Long create(Recipe recipe) {
        return jdbcClient.sql("""
                INSERT INTO recipe
                    (name, instructions, preparation_time_minutes, servings)
                VALUES
                    (:name, :instructions, :preparationTimeMinutes, :servings)
                RETURNING id
                """)
                .param("name", recipe.name())
                .param("instructions", recipe.instructions())
                .param("preparationTimeMinutes", recipe.preparationTimeMinutes())
                .param("servings", recipe.servings())
                .query(Long.class)
                .single();
    }
}
