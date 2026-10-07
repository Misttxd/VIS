package com.vis.rest;

import org.springframework.stereotype.Component;

@Component
public class IngredientDAOFactory {

    private final PostgresIngredientDao postgresDao;

    public IngredientDAOFactory(PostgresIngredientDao postgresDao) {
        this.postgresDao = postgresDao;
    }

    public IngredientDao create(String storageType) {
        if ("postgres".equalsIgnoreCase(storageType)) {
            return postgresDao;
        }

        if ("csv".equalsIgnoreCase(storageType)) {
            return new TextFileIngredientDao(); //ideálně sjednotit ať to je jedna možnost nebo druhá, protože jedna vytváří nový a kdo ví co
        }

        throw new IllegalArgumentException(
                "Spatne uloziste: " + storageType
        );
    }
}