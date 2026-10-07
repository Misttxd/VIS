package com.vis.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TextFileIngredientDao implements IngredientDao {

    private final Path file = Path.of("ingredients.csv");

    @Override
    public List<Ingredient> findAll() {
        try {
            if (Files.notExists(file)) {
                return List.of();
            }

            return Files.readAllLines(file).stream()
                    .map(line -> line.split(";"))
                    .map(value -> new Ingredient(
                            Long.valueOf(value[0]),
                            Long.valueOf(value[1]),
                            value[2],
                            value[3]
                    ))
                    .toList();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public Long create(Ingredient ingredient) {
        long id = findAll().size() + 1L;

        String row = "%d;%d;%s;%s%n".formatted(
                id,
                ingredient.categoryId(),
                ingredient.name(),
                ingredient.defaultUnit()
        );

        try {
            Files.writeString(
                    file,
                    row,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            return id;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}