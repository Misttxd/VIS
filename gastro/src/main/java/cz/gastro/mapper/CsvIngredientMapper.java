package cz.gastro.mapper;

import cz.gastro.model.Ingredient;
import java.math.BigDecimal;

public class CsvIngredientMapper {
    public Ingredient fromLine(String line) {
        String[] fields = line.split(";", -1);
        return new Ingredient(Long.valueOf(fields[0]), fields[1],
                Long.valueOf(fields[2]), fields[3], new BigDecimal(fields[4]));
    }

    public String toLine(long id, Ingredient item) {
        return String.join(";", Long.toString(id), item.name().trim(),
                item.categoryId().toString(), item.unit(), item.minimum().toPlainString());
    }
}
