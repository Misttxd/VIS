CREATE TABLE IF NOT EXISTS app_user (
    id SERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL
);

UPDATE app_user
SET username = 'user', password = 'user'
WHERE username = 'kuchar';

INSERT INTO app_user (username, password, role)
VALUES
    ('admin', 'admin', 'ADMIN'),
    ('user', 'user', 'USER')
ON CONFLICT (username) DO NOTHING;

CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (trim(name) <> '')
);
CREATE TABLE IF NOT EXISTS ingredient (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (trim(name) <> ''),
    category_id INTEGER NOT NULL REFERENCES category,
    unit TEXT NOT NULL CHECK (unit IN ('g','ml','ks')),
    minimum NUMERIC NOT NULL CHECK (minimum >= 0)
);
CREATE TABLE IF NOT EXISTS stock_delivery (
    id SERIAL PRIMARY KEY,
    ingredient_id INTEGER NOT NULL REFERENCES ingredient,
    remaining NUMERIC NOT NULL CHECK (remaining >= 0),
    expires DATE NOT NULL
);
CREATE TABLE IF NOT EXISTS recipe (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (trim(name) <> ''),
    instructions TEXT NOT NULL CHECK (trim(instructions) <> ''),
    servings INTEGER NOT NULL CHECK (servings > 0)
);
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    recipe_id INTEGER REFERENCES recipe,
    ingredient_id INTEGER REFERENCES ingredient,
    quantity NUMERIC NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (recipe_id, ingredient_id)
);
CREATE TABLE IF NOT EXISTS meal_plan (
    id SERIAL PRIMARY KEY,
    day DATE NOT NULL,
    prepared BOOLEAN NOT NULL DEFAULT FALSE
);
ALTER TABLE meal_plan ADD COLUMN IF NOT EXISTS prepared BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE meal_plan ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES app_user;

UPDATE meal_plan
SET user_id = (SELECT id FROM app_user WHERE username = 'admin')
WHERE user_id IS NULL;

ALTER TABLE meal_plan ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE meal_plan DROP CONSTRAINT IF EXISTS meal_plan_day_key;

CREATE UNIQUE INDEX IF NOT EXISTS meal_plan_user_day
ON meal_plan(user_id, day);

CREATE TABLE IF NOT EXISTS meal_plan_item (
    plan_id INTEGER REFERENCES meal_plan,
    recipe_id INTEGER REFERENCES recipe,
    portions INTEGER NOT NULL CHECK (portions > 0),
    PRIMARY KEY (plan_id, recipe_id)
);
