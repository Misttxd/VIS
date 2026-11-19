package cz.gastro.dao;

import cz.gastro.model.Category;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CategoryDao {
    private final JdbcClient db;

    public CategoryDao(JdbcClient db) {
        this.db = db;
    }

    public List<Category> getAllItems() {
        return db.sql("""
                SELECT id, name FROM category ORDER BY name
                """)
                .query(Category.class).list();
    }

    public long create(String name) {
        return db.sql("""
                INSERT INTO category(name) VALUES (?) RETURNING id
                """)
                .param(name.trim())
                .query(Long.class).single();
    }
}
