package cz.gastro.dao;

import cz.gastro.mapper.PostgresIngredientMapper;
import cz.gastro.model.Ingredient;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PostgresIngredientDao implements Dao<Ingredient> {
    private final PostgresIngredientMapper mapper = new PostgresIngredientMapper();
    private final JdbcClient db;

    public PostgresIngredientDao(JdbcClient db) {
        this.db = db;
    }

    @Override
    public List<Ingredient> getAllItems() {
        return db.sql("""
                SELECT * FROM ingredient ORDER BY name
                """)
                .query(mapper).list();
    }

    @Override
    public long create(Ingredient ingredient) {
        return db.sql("""
                INSERT INTO ingredient(name, category_id, unit, minimum)
                VALUES (?, ?, ?, ?) RETURNING id
                """)
                .param(ingredient.name().trim())
                .param(ingredient.categoryId())
                .param(ingredient.unit())
                .param(ingredient.minimum())
                .query(Long.class).single();
    }
}
