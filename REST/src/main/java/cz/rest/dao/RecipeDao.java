package cz.rest.dao;

import cz.rest.model.Recipe;
import cz.rest.model.RecipeDetailRow;
import cz.rest.mapper.RecipeDetailMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RecipeDao {
    private final JdbcClient db;
    private final IdentityMap identityMap;

    public RecipeDao(JdbcClient db, IdentityMap identityMap) {
        this.db = db;
        this.identityMap = identityMap;
    }

    public List<Recipe> getAllItems() {
        // return db.sql("""
        //         SELECT * FROM recipe ORDER BY name
        //         """)
        //         .query(Recipe.class).list();
        List<Recipe> cached = identityMap.getList(Recipe.class, "all");
        if (cached != null) {
            return cached;
        }
        List<Recipe> rows = db.sql("""
                SELECT * FROM recipe ORDER BY name
                """)
                .query(Recipe.class).list();
        return identityMap.rememberList(Recipe.class, "all", rows, Recipe::id);
    }

    public int create(Recipe recipe) {
        // return db.sql("""
        //         INSERT INTO recipe(name, instructions, servings) VALUES (?, ?, ?) RETURNING id
        //         """)
        //         .param(recipe.name().trim())
        //         .param(recipe.instructions())
        //         .param(recipe.servings())
        //         .query(Integer.class).single();
        Recipe saved = db.sql("""
                INSERT INTO recipe(name, instructions, servings) VALUES (?, ?, ?) RETURNING *
                """)
                .param(recipe.name().trim())
                .param(recipe.instructions())
                .param(recipe.servings())
                .query(Recipe.class).single();
        identityMap.put(Recipe.class, saved.id(), saved);
        identityMap.forgetList(Recipe.class, "all");
        return saved.id();
    }

    public List<RecipeDetailRow> findDetail(int id) {
        return db.sql("""
                SELECT r.id AS recipe_id, r.name AS recipe_name, r.instructions, r.servings,
                       i.id AS ingredient_id, i.name AS ingredient_name,
                       c.name AS category_name, i.unit, ri.quantity
                FROM recipe r
                LEFT JOIN recipe_ingredient ri ON ri.recipe_id = r.id
                LEFT JOIN ingredient i ON i.id = ri.ingredient_id
                LEFT JOIN category c ON c.id = i.category_id
                WHERE r.id = ?
                ORDER BY i.name
                """)
                .param(id)
                .query(new RecipeDetailMapper()).list();
    }
}
