package com.vis.rest;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeDetailMapper implements RowMapper<RecipeDetail> {

    @Override
    public RecipeDetail mapRow(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {
        return new RecipeDetail(
                resultSet.getLong("recipe_id"),
                resultSet.getString("recipe_name"),
                resultSet.getString("instructions"),
                resultSet.getInt("preparation_time_minutes"),
                resultSet.getInt("servings"),
                resultSet.getLong("ingredient_id"),
                resultSet.getString("ingredient_name"),
                resultSet.getBigDecimal("quantity"),
                resultSet.getString("unit"),
                resultSet.getBoolean("optional")
        );
    }
}