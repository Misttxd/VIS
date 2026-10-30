package com.vis.rest.mapper;

import com.vis.rest.model.RecipeDetail;

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
                resultSet.getObject("ingredient_id", Long.class),
                resultSet.getString("ingredient_name"),
                resultSet.getBigDecimal("quantity"),
                resultSet.getString("unit"),
                resultSet.getBoolean("optional")
        );
    }
}
