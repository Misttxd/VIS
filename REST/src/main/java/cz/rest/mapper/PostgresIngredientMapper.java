package cz.rest.mapper;

import cz.rest.model.Ingredient;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresIngredientMapper implements RowMapper<Ingredient> {
    @Override
    public Ingredient mapRow(ResultSet row, int rowNumber) throws SQLException {
        return new Ingredient(row.getInt("id"), row.getString("name"),
                row.getInt("category_id"), row.getString("unit"), row.getBigDecimal("minimum"));
    }
}
