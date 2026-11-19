package cz.gastro.mapper;

import cz.gastro.model.RecipeDetailRow;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class RecipeDetailMapper implements RowMapper<RecipeDetailRow> {
    @Override
    public RecipeDetailRow mapRow(ResultSet row, int rowNumber) throws SQLException {
        return new RecipeDetailRow(
                row.getLong("recipe_id"), row.getString("recipe_name"),
                row.getString("instructions"), row.getInt("servings"),
                row.getObject("ingredient_id", Long.class), row.getString("ingredient_name"),
                row.getString("category_name"), row.getString("unit"), row.getBigDecimal("quantity")
        );
    }
}
