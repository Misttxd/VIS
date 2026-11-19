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
    private final IdentityMap identityMap;

    public PostgresIngredientDao(JdbcClient db, IdentityMap identityMap) {
        this.db = db;
        this.identityMap = identityMap;
    }

    @Override
    public List<Ingredient> getAllItems() {
        // return db.sql("""
        //         SELECT * FROM ingredient ORDER BY name
        //         """)
        //         .query(mapper).list();
        List<Ingredient> cached = identityMap.getList(Ingredient.class, "postgres");
        if (cached != null) {
            return cached;
        }
        List<Ingredient> rows = db.sql("""
                SELECT * FROM ingredient ORDER BY name
                """)
                .query(mapper).list();
        return identityMap.rememberList(Ingredient.class, "postgres", rows, Ingredient::id);
    }

    @Override
    public long create(Ingredient ingredient) {
        // return db.sql("""
        //         INSERT INTO ingredient(name, category_id, unit, minimum)
        //         VALUES (?, ?, ?, ?) RETURNING id
        //         """)
        //         .param(ingredient.name().trim())
        //         .param(ingredient.categoryId())
        //         .param(ingredient.unit())
        //         .param(ingredient.minimum())
        //         .query(Long.class).single();
        Ingredient saved = db.sql("""
                INSERT INTO ingredient(name, category_id, unit, minimum)
                VALUES (?, ?, ?, ?) RETURNING *
                """)
                .param(ingredient.name().trim())
                .param(ingredient.categoryId())
                .param(ingredient.unit())
                .param(ingredient.minimum())
                .query(mapper).single();
        identityMap.put(Ingredient.class, saved.id(), saved);
        identityMap.forgetList(Ingredient.class, "postgres");
        return saved.id();
    }
}
