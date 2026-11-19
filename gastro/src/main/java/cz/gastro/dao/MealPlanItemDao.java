package cz.gastro.dao;

import cz.gastro.model.PlanLine;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MealPlanItemDao {
    private record LineId(long planId, long recipeId) {}

    private final JdbcClient db;
    private final IdentityMap identityMap;

    public MealPlanItemDao(JdbcClient db, IdentityMap identityMap) {
        this.db = db;
        this.identityMap = identityMap;
    }

    public List<PlanLine> forPlan(long id) {
        // return db.sql("""
        //         SELECT recipe_id, portions FROM meal_plan_item WHERE plan_id = ? ORDER BY recipe_id
        //         """)
        //         .param(id)
        //         .query(PlanLine.class).list();
        List<PlanLine> cached = identityMap.getList(PlanLine.class, id);
        if (cached != null) {
            return cached;
        }
        List<PlanLine> rows = db.sql("""
                SELECT recipe_id, portions FROM meal_plan_item WHERE plan_id = ? ORDER BY recipe_id
                """)
                .param(id)
                .query(PlanLine.class).list();
        return identityMap.rememberList(PlanLine.class, id, rows, row -> new LineId(id, row.recipeId()));
    }

    public int create(long planId, PlanLine line) {
        // return db.sql("""
        //         INSERT INTO meal_plan_item(plan_id, recipe_id, portions) VALUES (?, ?, ?)
        //         """)
        //         .param(planId)
        //         .param(line.recipeId())
        //         .param(line.portions())
        //         .update();
        int inserted = db.sql("""
                INSERT INTO meal_plan_item(plan_id, recipe_id, portions) VALUES (?, ?, ?)
                """)
                .param(planId)
                .param(line.recipeId())
                .param(line.portions())
                .update();
        identityMap.put(PlanLine.class, new LineId(planId, line.recipeId()), line);
        identityMap.forgetList(PlanLine.class, planId);
        return inserted;
    }
}
