package cz.rest.dao;

import cz.rest.model.RecipeLine;
import cz.rest.model.RecipeDetail;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RecipeIngredientDao {
    private record LineId(int recipeId, int ingredientId) {}

    private final JdbcClient db;
    private final IdentityMap identityMap;

    public RecipeIngredientDao(JdbcClient db, IdentityMap identityMap) {
        this.db = db;
        this.identityMap = identityMap;
    }

    public List<RecipeLine> forRecipe(int id) {
        // return db.sql("""
        //         SELECT ingredient_id, quantity FROM recipe_ingredient WHERE recipe_id = ? ORDER BY ingredient_id
        //         """)
        //         .param(id)
        //         .query(RecipeLine.class).list();
        List<RecipeLine> cached = identityMap.getList(RecipeLine.class, id);
        if (cached != null) {
            return cached;
        }
        List<RecipeLine> rows = db.sql("""
                SELECT ingredient_id, quantity FROM recipe_ingredient WHERE recipe_id = ? ORDER BY ingredient_id
                """)
                .param(id)
                .query(RecipeLine.class).list();
        return identityMap.rememberList(RecipeLine.class, id, rows, row -> new LineId(id, row.ingredientId()));
    }

    public int create(int recipeId, RecipeLine line) {
        // return db.sql("""
        //         INSERT INTO recipe_ingredient(recipe_id, ingredient_id, quantity) VALUES (?, ?, ?)
        //         """)
        //         .param(recipeId)
        //         .param(line.ingredientId())
        //         .param(line.quantity())
        //         .update();
        int inserted = db.sql("""
                INSERT INTO recipe_ingredient(recipe_id, ingredient_id, quantity) VALUES (?, ?, ?)
                """)
                .param(recipeId)
                .param(line.ingredientId())
                .param(line.quantity())
                .update();
        identityMap.put(RecipeLine.class, new LineId(recipeId, line.ingredientId()), line);
        identityMap.remove(RecipeDetail.class, recipeId);
        identityMap.forgetList(RecipeLine.class, recipeId);
        return inserted;
    }
}
