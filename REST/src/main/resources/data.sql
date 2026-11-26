-- Ukázková data pro první spuštění prázdné databáze.
BEGIN;

-- Prázdná pomocná tabulka přeskočí všechny zápisy při dalším spuštění.
CREATE TEMP TABLE seed_empty_database ON COMMIT DROP AS
SELECT 1 AS ready
WHERE NOT EXISTS (SELECT 1 FROM ingredient)
  AND NOT EXISTS (SELECT 1 FROM recipe)
  AND NOT EXISTS (SELECT 1 FROM meal_plan);

INSERT INTO category (name)
SELECT data.name
FROM (VALUES ('Zelenina'), ('Ovoce'), ('Mléčné výrobky'), ('Obiloviny'),
             ('Maso'), ('Základní suroviny')) AS data(name)
CROSS JOIN seed_empty_database
ON CONFLICT (name) DO NOTHING;

INSERT INTO ingredient (name, category_id, unit, minimum)
SELECT data.name, c.id, data.unit, data.minimum
FROM (VALUES
    ('Brambory', 'Zelenina', 'g', 1000),
    ('Mrkev', 'Zelenina', 'g', 500),
    ('Cibule', 'Zelenina', 'g', 300),
    ('Česnek', 'Zelenina', 'g', 100),
    ('Jablka', 'Ovoce', 'g', 500),
    ('Mléko', 'Mléčné výrobky', 'ml', 1000),
    ('Máslo', 'Mléčné výrobky', 'g', 200),
    ('Eidam', 'Mléčné výrobky', 'g', 200),
    ('Pšeničná mouka', 'Obiloviny', 'g', 1000),
    ('Ovesné vločky', 'Obiloviny', 'g', 500),
    ('Rýže', 'Obiloviny', 'g', 1000),
    ('Kuřecí prsa', 'Maso', 'g', 1000),
    ('Vejce', 'Základní suroviny', 'ks', 6),
    ('Olej', 'Základní suroviny', 'ml', 500),
    ('Sůl', 'Základní suroviny', 'g', 100)
) AS data(name, category, unit, minimum)
JOIN category c ON c.name = data.category
CROSS JOIN seed_empty_database;

INSERT INTO stock_delivery (ingredient_id, remaining, expires)
SELECT i.id, data.quantity, CURRENT_DATE + data.days
FROM (VALUES
    ('Brambory', 5000, 14),
    ('Brambory', 1000, 2),
    ('Mrkev', 1200, 7),
    ('Cibule', 1500, 21),
    ('Česnek', 150, 30),
    ('Jablka', 1800, 5),
    ('Mléko', 2400, 2),
    ('Máslo', 400, 10),
    ('Eidam', 150, 4),
    ('Pšeničná mouka', 3000, 90),
    ('Ovesné vločky', 1000, 60),
    ('Rýže', 2500, 180),
    ('Kuřecí prsa', 800, 1),
    ('Vejce', 12, 12),
    ('Olej', 1000, 180),
    ('Sůl', 500, 365)
) AS data(name, quantity, days)
JOIN ingredient i ON i.name = data.name
CROSS JOIN seed_empty_database;

INSERT INTO recipe (name, instructions, servings)
SELECT data.name, data.instructions, data.servings
FROM (VALUES
    ('Ovesná kaše s jablky',
     'Vločky povařte v mléce do změknutí. Přidejte nastrouhaná jablka a krátce prohřejte.', 4),
    ('Bramborová kaše',
     'Oloupané brambory uvařte v osolené vodě. Slijte a rozšťouchejte s máslem a teplým mlékem.', 4),
    ('Kuřecí rizoto',
     'Na oleji orestujte cibuli, kuřecí maso a mrkev. Přidejte rýži, osolte, zalijte vodou a duste doměkka.', 4),
    ('Bramboráky',
     'Brambory nastrouhejte a vymačkejte přebytečnou vodu. Vmíchejte vejce, mouku, česnek a sůl. Smažte na oleji.', 4),
    ('Zapečené brambory',
     'Předvařené brambory nakrájejte do zapékací mísy. Zalijte vejci rozmíchanými s mlékem a solí, posypte sýrem a upečte.', 4)
) AS data(name, instructions, servings)
CROSS JOIN seed_empty_database;

INSERT INTO recipe_ingredient (recipe_id, ingredient_id, quantity)
SELECT r.id, i.id, data.quantity
FROM (VALUES
    ('Ovesná kaše s jablky', 'Ovesné vločky', 200),
    ('Ovesná kaše s jablky', 'Mléko', 800),
    ('Ovesná kaše s jablky', 'Jablka', 300),
    ('Bramborová kaše', 'Brambory', 1000),
    ('Bramborová kaše', 'Mléko', 200),
    ('Bramborová kaše', 'Máslo', 60),
    ('Bramborová kaše', 'Sůl', 5),
    ('Kuřecí rizoto', 'Rýže', 320),
    ('Kuřecí rizoto', 'Kuřecí prsa', 500),
    ('Kuřecí rizoto', 'Cibule', 100),
    ('Kuřecí rizoto', 'Mrkev', 150),
    ('Kuřecí rizoto', 'Olej', 30),
    ('Kuřecí rizoto', 'Sůl', 5),
    ('Bramboráky', 'Brambory', 1000),
    ('Bramboráky', 'Pšeničná mouka', 120),
    ('Bramboráky', 'Vejce', 2),
    ('Bramboráky', 'Česnek', 20),
    ('Bramboráky', 'Olej', 80),
    ('Bramboráky', 'Sůl', 8),
    ('Zapečené brambory', 'Brambory', 1000),
    ('Zapečené brambory', 'Vejce', 3),
    ('Zapečené brambory', 'Mléko', 150),
    ('Zapečené brambory', 'Eidam', 150),
    ('Zapečené brambory', 'Sůl', 5)
) AS data(recipe, ingredient, quantity)
JOIN recipe r ON r.name = data.recipe
JOIN ingredient i ON i.name = data.ingredient
CROSS JOIN seed_empty_database;

INSERT INTO meal_plan (day, user_id)
SELECT CURRENT_DATE, id FROM app_user
CROSS JOIN seed_empty_database
WHERE username IN ('admin', 'user');

INSERT INTO meal_plan_item (plan_id, recipe_id, portions)
SELECT p.id, r.id, data.portions
FROM (VALUES
    ('admin', 'Bramboráky', 4),
    ('admin', 'Zapečené brambory', 4),
    ('user', 'Kuřecí rizoto', 8),
    ('user', 'Ovesná kaše s jablky', 4)
) AS data(username, recipe, portions)
JOIN app_user u ON u.username = data.username
JOIN meal_plan p ON p.user_id = u.id AND p.day = CURRENT_DATE
JOIN recipe r ON r.name = data.recipe
CROSS JOIN seed_empty_database;

COMMIT;
