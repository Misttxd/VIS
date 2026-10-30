package com.vis.rest.repository;

import com.vis.rest.model.IngredientCategory;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class IngredientCategoryRepository {

    private final JdbcClient jdbcClient;

    public IngredientCategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long create(IngredientCategory category) {
        return jdbcClient.sql("""
                INSERT INTO ingredient_category (name)
                VALUES (:name)
                RETURNING id
                """)
                .param("name", category.name())
                .query(Long.class)
                .single();
    }
}
