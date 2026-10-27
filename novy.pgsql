SELECT id, name
FROM recipe;

SELECT id, name, default_unit
FROM ingredient
ORDER BY id;

SELECT *
FROM recipe_ingredient;

SELECT
    r.name AS recipe,
    i.name AS ingredient,
    ri.quantity,
    ri.unit,
    ri.optional
FROM recipe r
JOIN recipe_ingredient ri ON ri.recipe_id = r.id
JOIN ingredient i ON i.id = ri.ingredient_id
WHERE r.id = 1
ORDER BY i.name;¨

SELECT *
FROM recipe_ingredient
WHERE recipe_id = 1;