package cz.gastro.dao;

import cz.gastro.model.MealPlan;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public class MealPlanDao {
//    private final JdbcClient db;
//
//    public MealPlanDao(JdbcClient db) {
//        this.db = db;
//    }

    private final JdbcClient db;
    // private final MealPlanIdentityMap identityMap;
    private final IdentityMap identityMap;

    public MealPlanDao(JdbcClient db, IdentityMap identityMap) {
        this.db = db;
        this.identityMap = identityMap;
    }



//    public List<MealPlan> getAllItems() {
//        return db.sql("""
//                SELECT * FROM meal_plan ORDER BY day DESC
//                """)
//                .query(MealPlan.class).list();
//    }

    public List<MealPlan> getAllItems() {
        List<MealPlan> cached = identityMap.getList(MealPlan.class, "all");
        if (cached != null) {
            return cached;
        }
        List<MealPlan> plans = db.sql("SELECT * FROM meal_plan ORDER BY day DESC")
                .query(MealPlan.class).list();
        return identityMap.rememberList(MealPlan.class, "all", plans, MealPlan::id);
    }

    public long create(LocalDate day) {
        // Původně INSERT vracel jen ID (RETURNING id).
        MealPlan saved = db.sql("""
                INSERT INTO meal_plan(day) VALUES (?) ON CONFLICT (day) DO NOTHING RETURNING *
                """)
                .param(day)
                .query(MealPlan.class).optional()
                .orElseThrow(() -> new IllegalArgumentException("Pro tento den už plán existuje. Vyber jiné datum."));
        identityMap.put(MealPlan.class, saved.id(), saved);
        identityMap.forgetList(MealPlan.class, "all");
        return saved.id();
    }


//    public MealPlan getById(long id) {
//        return db.sql("SELECT * FROM meal_plan WHERE id = ?")
//                .param(id).query(MealPlan.class).optional()
//                .orElseThrow(() -> new IllegalArgumentException("Plán neexistuje."));
//    }

    public MealPlan getById(long id) {
        MealPlan plan = identityMap.get(MealPlan.class, id);
        if (plan != null) {
            return plan;
        }

        plan = db.sql("SELECT * FROM meal_plan WHERE id = ?")
                .param(id)
                .query(MealPlan.class)
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("Plán neexistuje."));

        identityMap.put(MealPlan.class, id, plan);
        return plan;
    }

    public MealPlan lockForPreparation(long id) {
        MealPlan locked = db.sql("SELECT * FROM meal_plan WHERE id = ? FOR UPDATE")
                .param(id).query(MealPlan.class).optional()
                .orElseThrow(() -> new IllegalArgumentException("Plán neexistuje."));
        return identityMap.remember(MealPlan.class, id, locked);
    }

//    public void markPrepared(long id) {
//        db.sql("UPDATE meal_plan SET prepared = TRUE WHERE id = ?")
//                .param(id).update();
//    }

    public void markPrepared(long id) {
        MealPlan updatedPlan = db.sql("""
            UPDATE meal_plan SET prepared = TRUE WHERE id = ? RETURNING *""")
            .param(id)
            .query(MealPlan.class)
            .single();

        identityMap.put(MealPlan.class, id, updatedPlan);
    }
}
