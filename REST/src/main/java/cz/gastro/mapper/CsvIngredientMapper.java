package cz.gastro.mapper;

import cz.gastro.model.Ingredient;
import java.math.BigDecimal;

public class CsvIngredientMapper {
    public Ingredient fromLine(String line) {
        String[] fields = line.split(";", -1);
        return new Ingredient(Integer.valueOf(fields[0]), fields[1],
                Integer.valueOf(fields[2]), fields[3], new BigDecimal(fields[4]));
    }

    public String toLine(int id, Ingredient item) {
        return String.join(";", Integer.toString(id), item.name().trim(),
                item.categoryId().toString(), item.unit(), item.minimum().toPlainString());
    }
}
