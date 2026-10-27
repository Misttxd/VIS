package com.vis.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TextFileIngredientDao implements IngredientDao {

    private final Path file;
    private final CsvIngredientMapper mapper;

    public TextFileIngredientDao(Path file, CsvIngredientMapper mapper) {
        this.file = file;
        this.mapper = mapper;
    }

    @Override
    public List<Ingredient> getAllItems() {
        try {
            if (Files.notExists(file)) {
                return List.of();
            }

            return Files.readAllLines(file).stream()
                    .filter(line -> !line.isBlank())
                    .map(mapper::fromLine)
                    .toList();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public Long create(Ingredient ingredient) {
        long id = getAllItems().stream()
                .mapToLong(Ingredient::id)
                .max()
                .orElse(0) + 1;

        Ingredient storedIngredient = new Ingredient(
                id,
                ingredient.categoryId(),
                ingredient.name(),
                ingredient.defaultUnit()
        );

        try {
            String separator = "";

            if (Files.exists(file) && Files.size(file) > 0) {
                String content = Files.readString(file);

                if (!content.endsWith("\n") && !content.endsWith("\r")) {
                    separator = System.lineSeparator();
                }
            }

            Files.writeString(
                    file,
                    separator + mapper.toLine(storedIngredient) + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            return id;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
