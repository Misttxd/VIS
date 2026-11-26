package cz.rest.factory;

import cz.rest.dao.CsvIngredientDao;
import cz.rest.dao.Dao;
import cz.rest.dao.PostgresIngredientDao;
import cz.rest.model.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientDaoFactory {
    private final PostgresIngredientDao postgres;
    private final CsvIngredientDao csv;

    public IngredientDaoFactory(PostgresIngredientDao postgres, CsvIngredientDao csv) {
        this.postgres = postgres;
        this.csv = csv;
    }

    public Dao<Ingredient> select(String storage) {
        if (storage.equals("postgres")) {
            return postgres;
        }
        if (storage.equals("csv")) {
            return csv;
        }
        throw new IllegalArgumentException("Neznámé úložiště.");
    }
}
