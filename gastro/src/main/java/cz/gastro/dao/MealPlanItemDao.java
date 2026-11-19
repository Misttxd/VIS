package cz.gastro.dao;

import cz.gastro.model.PlanLine;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MealPlanItemDao {
    private final JdbcClient db;

    public MealPlanItemDao(JdbcClient db) {
        this.db = db;
    }

    public List<PlanLine> forPlan(long id) {
        return db.sql("""
                SELECT recipe_id, portions FROM meal_plan_item WHERE plan_id = ? ORDER BY recipe_id
                """)
                .param(id)
                .query(PlanLine.class).list();
    }

    public int create(long planId, PlanLine line) {
        return db.sql("""
                INSERT INTO meal_plan_item(plan_id, recipe_id, portions) VALUES (?, ?, ?)
                """)
                .param(planId)
                .param(line.recipeId())
                .param(line.portions())
                .update();
    }
}
