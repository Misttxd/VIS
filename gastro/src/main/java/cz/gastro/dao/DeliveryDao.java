package cz.gastro.dao;

import cz.gastro.model.Delivery;
import cz.gastro.model.StockLevel;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public class DeliveryDao {
    private final JdbcClient db;

    public DeliveryDao(JdbcClient db) {
        this.db = db;
    }

    public List<Delivery> getAllItems() {
        return db.sql("""
                SELECT * FROM stock_delivery ORDER BY expires, id
                """)
                .query(Delivery.class).list();
    }

    public List<Delivery> remainingDeliveries() {
        return db.sql("""
                SELECT * FROM stock_delivery WHERE remaining > 0 ORDER BY expires, id
                """)
                .query(Delivery.class).list();
    }

    public List<StockLevel> levels(LocalDate day) {
        return db.sql("""
                SELECT i.id AS ingredient_id, i.name, i.unit,
                       COALESCE(SUM(d.remaining), 0) AS available,
                       (COUNT(d.id) > 0 AND SUM(d.remaining) < i.minimum) AS low
                FROM ingredient i
                LEFT JOIN stock_delivery d ON d.ingredient_id = i.id
                    AND d.remaining > 0 AND d.expires >= ?
                GROUP BY i.id
                ORDER BY i.name
                """)
                .param(day).query(StockLevel.class).list();
    }

    public long receive(Delivery delivery) {
        return db.sql("""
                INSERT INTO stock_delivery(ingredient_id, remaining, expires)
                VALUES (?, ?, ?) RETURNING id
                """)
                .param(delivery.ingredientId())
                .param(delivery.remaining())
                .param(delivery.expires())
                .query(Long.class).single();
    }

    @Transactional
    public int withdraw(long id, BigDecimal quantity) {
        int updated = db.sql("""
                UPDATE stock_delivery SET remaining = remaining - :quantity
                WHERE id = :id AND remaining >= :quantity
                """)
                .param("quantity", quantity)
                .param("id", id)
                .update();
        if (updated > 0) {
            db.sql("DELETE FROM stock_delivery WHERE id = ? AND remaining = 0")
                    .param(id).update();
        }
        return updated;
    }

    public List<Delivery> lockUsable(long ingredientId, LocalDate day) {
        return db.sql("""
                SELECT * FROM stock_delivery
                WHERE ingredient_id = ? AND remaining > 0 AND expires >= ?
                ORDER BY expires, id
                FOR UPDATE
                """)
                .param(ingredientId).param(day)
                .query(Delivery.class).list();
    }
}
