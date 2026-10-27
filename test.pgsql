INSERT INTO recipe (
    name,
    instructions,
    preparation_time_minutes,
    servings
)
VALUES (
    'Bramboráky',
    'Smíchat suroviny a osmažit.',
    30,
    4
)
RETURNING id;

INSERT INTO ingredient (name, default_unit)
VALUES ('Brambory', 'g')
RETURNING id;

INSERT INTO ingredient (name, default_unit)
VALUES ('Mouka', 'g')
RETURNING id;

INSERT INTO ingredient (name, default_unit)
VALUES ('rohlikova_ingredience', 'g')
RETURNING id;



INSERT INTO recipe_ingredient (
    recipe_id,
    ingredient_id,
    quantity,
    unit,
    optional
)
VALUES
    (1, 2, 1000, 'g', false),
    (1, 3, 150, 'g', false);