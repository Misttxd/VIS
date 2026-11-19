package cz.gastro.dao;

import cz.gastro.model.MealPlan;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public class MealPlanDao {
    private final JdbcClient db;

    public MealPlanDao(JdbcClient db) {
        this.db = db;
    }

    public List<MealPlan> getAllItems() {
        return db.sql("""
                SELECT * FROM meal_plan ORDER BY day DESC
                """)
                .query(MealPlan.class).list();
    }

    public long create(LocalDate day) {
        return db.sql("""
                INSERT INTO meal_plan(day) VALUES (?) ON CONFLICT (day) DO NOTHING RETURNING id
                """)
                .param(day)
                .query(Long.class).optional()
                .orElseThrow(() -> new IllegalArgumentException("Pro tento den už plán existuje. Vyber jiné datum."));
    }

    public LocalDate day(long id) {
        return db.sql("""
                SELECT day FROM meal_plan WHERE id = ?
                """)
                .param(id)
                .query(LocalDate.class).single();
    }

    public MealPlan getById(long id) {
        return db.sql("SELECT * FROM meal_plan WHERE id = ?")
                .param(id).query(MealPlan.class).optional()
                .orElseThrow(() -> new IllegalArgumentException("Plán neexistuje."));
    }

    public MealPlan lockForPreparation(long id) {
        return db.sql("SELECT * FROM meal_plan WHERE id = ? FOR UPDATE")
                .param(id).query(MealPlan.class).optional()
                .orElseThrow(() -> new IllegalArgumentException("Plán neexistuje."));
    }

    public void markPrepared(long id) {
        db.sql("UPDATE meal_plan SET prepared = TRUE WHERE id = ?")
                .param(id).update();
    }
}
