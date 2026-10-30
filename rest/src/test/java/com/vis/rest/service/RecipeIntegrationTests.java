package com.vis.rest.service;

import com.vis.rest.dto.RecipeCreateRequest;
import com.vis.rest.dto.RecipeIngredientRequest;
import com.vis.rest.model.RecipeDetail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RecipeIntegrationTests {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void recipeWithoutIngredientsIsStillFound() {
        Long id = recipeService.create(request(List.of()));

        List<RecipeDetail> detail = recipeService.getDetailById(id);

        assertEquals(1, detail.size());
        assertEquals(id, detail.getFirst().recipeId());
        assertNull(detail.getFirst().ingredientId());
    }

    @Test
    void recipeIsSavedAndLoadedWithItsIngredients() {
        Long firstId = createIngredient();
        Long secondId = createIngredient();
        Long recipeId = recipeService.create(request(List.of(
                new RecipeIngredientRequest(firstId, null, new BigDecimal("100"), "g", false),
                new RecipeIngredientRequest(secondId, null, new BigDecimal("50"), "g", true)
        )));

        List<RecipeDetail> detail = recipeService.getDetailById(recipeId);

        assertEquals(2, detail.size());
        RecipeDetail first = detail.stream().filter(row -> row.ingredientId().equals(firstId)).findFirst().orElseThrow();
        RecipeDetail second = detail.stream().filter(row -> row.ingredientId().equals(secondId)).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("100").compareTo(first.quantity()));
        assertFalse(first.optional());
        assertEquals(0, new BigDecimal("50").compareTo(second.quantity()));
        assertTrue(second.optional());
    }

    @Test
    void duplicateIngredientsRollBackWholeRecipe() {
        String name = "Duplicate-" + UUID.randomUUID();
        RecipeIngredientRequest item = new RecipeIngredientRequest(null, name, BigDecimal.ONE, "g", false);
        RecipeCreateRequest request = request(List.of(item, item));

        assertThrows(DuplicateKeyException.class, () -> createInSeparateTransaction(request));

        assertEquals(0L, jdbcClient.sql("SELECT count(*) FROM recipe WHERE name = :name")
                .param("name", request.name()).query(Long.class).single());
        assertEquals(0L, jdbcClient.sql("SELECT count(*) FROM ingredient WHERE name = :name")
                .param("name", name).query(Long.class).single());
    }

    @Test
    void recipeCanCombineExistingAndNewIngredients() {
        Long existingId = createIngredient();
        String newName = "New-" + UUID.randomUUID();
        Long recipeId = recipeService.create(request(List.of(
                new RecipeIngredientRequest(existingId, null, BigDecimal.ONE, "g", false),
                new RecipeIngredientRequest(null, newName, new BigDecimal("25"), "g", true)
        )));

        List<RecipeDetail> detail = recipeService.getDetailById(recipeId);
        assertEquals(2, detail.size());
        assertTrue(detail.stream().anyMatch(row -> row.ingredientId().equals(existingId)));
        RecipeDetail newItem = detail.stream().filter(row -> row.ingredientName().equals(newName))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("25").compareTo(newItem.quantity()));
        assertTrue(newItem.optional());
        assertEquals("g", jdbcClient.sql("SELECT default_unit FROM ingredient WHERE id = :id")
                .param("id", newItem.ingredientId()).query(String.class).single());
    }

    @Test
    void typedExistingNameReusesIngredient() {
        Long existingId = createIngredient();
        String name = jdbcClient.sql("SELECT name FROM ingredient WHERE id = :id")
                .param("id", existingId).query(String.class).single();
        Long recipeId = recipeService.create(request(List.of(
                new RecipeIngredientRequest(null, " " + name.toUpperCase() + " ", BigDecimal.ONE, "g", false)
        )));

        assertEquals(existingId, recipeService.getDetailById(recipeId).getFirst().ingredientId());
        assertEquals(1L, jdbcClient.sql("SELECT count(*) FROM ingredient WHERE lower(name) = lower(:name)")
                .param("name", name).query(Long.class).single());
    }

    @Test
    void invalidQuantityRollsBackRecipeAndNewIngredient() {
        String name = "Rollback-" + UUID.randomUUID();
        RecipeCreateRequest request = request(List.of(
                new RecipeIngredientRequest(null, name, BigDecimal.ZERO, "g", false)
        ));

        assertThrows(DataIntegrityViolationException.class, () -> createInSeparateTransaction(request));
        assertEquals(0L, jdbcClient.sql("SELECT count(*) FROM recipe WHERE name = :name")
                .param("name", request.name()).query(Long.class).single());
        assertEquals(0L, jdbcClient.sql("SELECT count(*) FROM ingredient WHERE name = :name")
                .param("name", name).query(Long.class).single());
    }

    private Long createInSeparateTransaction(RecipeCreateRequest request) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transaction.execute(status -> recipeService.create(request));
    }

    private RecipeCreateRequest request(List<RecipeIngredientRequest> ingredients) {
        return new RecipeCreateRequest("Test-" + UUID.randomUUID(), "Testovací postup", 10, 2, ingredients);
    }

    private Long createIngredient() {
        return jdbcClient.sql("INSERT INTO ingredient (name, default_unit) VALUES (:name, 'g') RETURNING id")
                .param("name", "Test-" + UUID.randomUUID()).query(Long.class).single();
    }
}
