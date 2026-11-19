package cz.gastro.mapper;

import cz.gastro.model.Ingredient;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresIngredientMapper implements RowMapper<Ingredient> {
    @Override
    public Ingredient mapRow(ResultSet row, int rowNumber) throws SQLException {
        return new Ingredient(row.getLong("id"), row.getString("name"),
                row.getLong("category_id"), row.getString("unit"), row.getBigDecimal("minimum"));
    }
}
