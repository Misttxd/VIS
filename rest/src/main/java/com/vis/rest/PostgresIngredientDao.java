package com.vis.rest;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostgresIngredientDao implements IngredientDao {

    private final JdbcClient jdbcClient;

    public PostgresIngredientDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Ingredient> findAll() {
        return jdbcClient.sql("""
                SELECT id, category_id, name, default_unit
                FROM ingredient
                ORDER BY name
                """)
                .query(Ingredient.class)
                .list();
    }

    @Override
    public Long create(Ingredient ingredient) {
        return jdbcClient.sql("""
                INSERT INTO ingredient (category_id, name, default_unit)
                VALUES (:categoryId, :name, :defaultUnit)
                RETURNING id
                """)
                .param("categoryId", ingredient.categoryId())
                .param("name", ingredient.name())
                .param("defaultUnit", ingredient.defaultUnit())
                .query(Long.class)
                .single();
    }
}
