package com.vis.rest.dao;

import com.vis.rest.mapper.PostgresIngredientMapper;
import com.vis.rest.model.Ingredient;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Optional;

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
    public Optional<Ingredient> findByName(String name) {
        return jdbcClient.sql("""
                SELECT id, category_id, name, default_unit
                FROM ingredient
                WHERE lower(name) = lower(:name)
                ORDER BY id
                LIMIT 1
                """)
                .param("name", name.trim())
                .query(mapper)
                .optional();
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


