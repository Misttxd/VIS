package cz.gastro.factory;

import cz.gastro.dao.CsvIngredientDao;
import cz.gastro.dao.Dao;
import cz.gastro.dao.PostgresIngredientDao;
import cz.gastro.model.Ingredient;
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
