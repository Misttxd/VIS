package com.vis.rest;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class StockBatchRepository {

    private final JdbcClient jdbcClient;

    public StockBatchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long create(StockBatch batch) {
        return jdbcClient.sql("""
                INSERT INTO stock_batch
                    (ingredient_id, quantity, expiration_date, received_date)
                VALUES
                    (:ingredientId, :quantity, :expirationDate, :receivedDate)
                RETURNING id
                """)
                .param("ingredientId", batch.ingredientId())
                .param("quantity", batch.quantity())
                .param("expirationDate", batch.expirationDate())
                .param("receivedDate", batch.receivedDate())
                .query(Long.class)
                .single();
    }
}
