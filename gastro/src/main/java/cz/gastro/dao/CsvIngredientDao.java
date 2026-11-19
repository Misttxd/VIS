package cz.gastro.dao;

import cz.gastro.mapper.CsvIngredientMapper;
import cz.gastro.model.Ingredient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CsvIngredientDao implements Dao<Ingredient> {
    private final CsvIngredientMapper mapper = new CsvIngredientMapper();
    private record CsvId(long id) {}

    private final Path file;
    private final IdentityMap identityMap;

    public CsvIngredientDao(@Value("${gastro.ingredient-file}") String filename, IdentityMap identityMap) {
        this.identityMap = identityMap;
        this.file = Path.of(filename);
    }

    @Override
    public synchronized List<Ingredient> getAllItems() {
        List<Ingredient> cached = identityMap.getList(Ingredient.class, "csv");
        if (cached != null) {
            return cached;
        }
        return identityMap.rememberList(Ingredient.class, "csv", readFile(), item -> new CsvId(item.id()));
    }

    private List<Ingredient> readFile() {
        List<Ingredient> ingredients = new ArrayList<>();
        if (Files.notExists(file)) {
            return ingredients;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                ingredients.add(mapper.fromLine(line));
            }
            return ingredients;
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    @Override
    public synchronized long create(Ingredient item) {
        String name = item.name().trim();
        if (name.contains(";") || name.contains("\n") || name.contains("\r")) {
            throw new IllegalArgumentException("Název v CSV musí být na jednom řádku bez středníku.");
        }
        long nextId = 1;
        // Při zápisu ověřujeme duplicity a další ID podle aktuálního souboru.
        for (Ingredient existing : readFile()) {
            if (existing.name().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Surovina již v CSV existuje.");
            }
            nextId = Math.max(nextId, existing.id() + 1);
        }
        String line = mapper.toLine(nextId, item);
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            identityMap.put(Ingredient.class, new CsvId(nextId), mapper.fromLine(line));
            identityMap.forgetList(Ingredient.class, "csv");
            return nextId;
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }
}
