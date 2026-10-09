package com.vis.rest;

import org.springframework.jdbc.core.simple.JdbcClient;
import java.util.List;

public class PostgresIngredientDao implements IngredientDao {

    private final JdbcClient jdbcClient;
    private final PostgresIngredientMapper mapper;

    public PostgresIngredientDao(JdbcClient jdbcClient, PostgresIngredientMapper mapper) {
        this.jdbcClient = jdbcClient;
        this.mapper = mapper;
    }

    @Override
    public List<Ingredient> getAllItems() {
        return jdbcClient.sql("""
                SELECT id, category_id, name, default_unit
                FROM ingredient
                ORDER BY name
                """)
                .query(mapper)
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
