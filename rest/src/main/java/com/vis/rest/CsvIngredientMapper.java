package com.vis.rest;

public class CsvIngredientMapper {

    public Ingredient fromLine(String line) {
        String[] values = line.split(";", -1);

        if (values.length != 4) {
            throw new IllegalArgumentException("Neplatný CSV řádek: " + line);
        }

        Long categoryId = values[1].isBlank() ? null : Long.valueOf(values[1]);

        return new Ingredient(
                Long.valueOf(values[0]),
                categoryId,
                values[2],
                values[3]
        );
    }

    public String toLine(Ingredient ingredient) {
        String categoryId = ingredient.categoryId() == null
                ? ""
                : ingredient.categoryId().toString();

        return String.join(";",
                ingredient.id().toString(),
                categoryId,
                ingredient.name(),
                ingredient.defaultUnit()
        );
    }
}
