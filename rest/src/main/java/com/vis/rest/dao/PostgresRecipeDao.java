package com.vis.rest.dao;

import com.vis.rest.model.Recipe;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresRecipeDao {

    private final JdbcClient jdbcClient;

    public PostgresRecipeDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
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
