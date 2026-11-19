package cz.gastro.dao;

import cz.gastro.model.RecipeLine;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RecipeIngredientDao {
    private final JdbcClient db;

    public RecipeIngredientDao(JdbcClient db) {
        this.db = db;
    }

    public List<RecipeLine> forRecipe(long id) {
        return db.sql("""
                SELECT ingredient_id, quantity FROM recipe_ingredient WHERE recipe_id = ? ORDER BY ingredient_id
                """)
                .param(id)
                .query(RecipeLine.class).list();
    }

    public int create(long recipeId, RecipeLine line) {
        return db.sql("""
                INSERT INTO recipe_ingredient(recipe_id, ingredient_id, quantity) VALUES (?, ?, ?)
                """)
                .param(recipeId)
                .param(line.ingredientId())
                .param(line.quantity())
                .update();
    }
}
