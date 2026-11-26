package cz.gastro.dao;

import cz.gastro.model.Category;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CategoryDao {
    private final JdbcClient db;
    private final IdentityMap identityMap;

    public CategoryDao(JdbcClient db, IdentityMap identityMap) {
        this.db = db;
        this.identityMap = identityMap;
    }

    public List<Category> getAllItems() {
        // return db.sql("""
        //         SELECT id, name FROM category ORDER BY name
        //         """)
        //         .query(Category.class).list();
        List<Category> cached = identityMap.getList(Category.class, "all");
        if (cached != null) {
            return cached;
        }
        List<Category> rows = db.sql("""
                SELECT id, name FROM category ORDER BY name
                """)
                .query(Category.class).list();
        return identityMap.rememberList(Category.class, "all", rows, Category::id);
    }

    public int create(String name) {
        // return db.sql("""
        //         INSERT INTO category(name) VALUES (?) RETURNING id
        //         """)
        //         .param(name.trim())
        //         .query(Integer.class).single();
        Category saved = db.sql("""
                INSERT INTO category(name) VALUES (?) RETURNING *
                """)
                .param(name.trim())
                .query(Category.class).single();
        identityMap.put(Category.class, saved.id(), saved);
        identityMap.forgetList(Category.class, "all");
        return saved.id();
    }
}
