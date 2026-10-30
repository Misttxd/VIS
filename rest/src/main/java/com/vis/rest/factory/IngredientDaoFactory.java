package com.vis.rest.factory;

import com.vis.rest.dao.CsvIngredientDao;
import com.vis.rest.dao.IngredientDao;
import com.vis.rest.dao.PostgresIngredientDao;
import com.vis.rest.mapper.CsvIngredientMapper;
import com.vis.rest.mapper.PostgresIngredientMapper;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class IngredientDaoFactory {

    private final IngredientDao postgresDao;
    private final IngredientDao csvDao;

    public IngredientDaoFactory(JdbcClient jdbcClient) {
        this.postgresDao = new PostgresIngredientDao(
                jdbcClient,
                new PostgresIngredientMapper()
        );
        this.csvDao = new CsvIngredientDao(
                Path.of("ingredients.csv"),
                new CsvIngredientMapper()
        );
    }

    @Bean
    public IngredientDao ingredientDao(
            @Value("${storage.type}") String storageType
    ) {
        return create(storageType);
    }

    public IngredientDao create(String storageType) {
        if ("postgres".equalsIgnoreCase(storageType)) {
            return postgresDao;
        }

        if ("csv".equalsIgnoreCase(storageType)) {
            return csvDao;
        }

        throw new IllegalArgumentException(
                "Špatné úložiště: " + storageType
        );
    }
}
