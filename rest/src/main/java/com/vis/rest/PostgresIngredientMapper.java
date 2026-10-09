package com.vis.rest;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresIngredientMapper implements RowMapper<Ingredient> {

    @Override
    public Ingredient mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Ingredient(
                resultSet.getLong("id"),
                resultSet.getObject("category_id", Long.class),
                resultSet.getString("name"),
                resultSet.getString("default_unit")
        );
    }
}
