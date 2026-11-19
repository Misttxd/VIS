CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (trim(name) <> '')
);
CREATE TABLE IF NOT EXISTS ingredient (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (trim(name) <> ''),
    category_id BIGINT NOT NULL REFERENCES category,
    unit TEXT NOT NULL CHECK (unit IN ('g','ml','ks')),
    minimum NUMERIC NOT NULL CHECK (minimum >= 0)
);
CREATE TABLE IF NOT EXISTS stock_delivery (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL REFERENCES ingredient,
    remaining NUMERIC NOT NULL CHECK (remaining >= 0),
    expires DATE NOT NULL
);
CREATE TABLE IF NOT EXISTS recipe (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (trim(name) <> ''),
    instructions TEXT NOT NULL CHECK (trim(instructions) <> ''),
    servings INTEGER NOT NULL CHECK (servings > 0)
);
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    recipe_id BIGINT REFERENCES recipe,
    ingredient_id BIGINT REFERENCES ingredient,
    quantity NUMERIC NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (recipe_id, ingredient_id)
);
CREATE TABLE IF NOT EXISTS meal_plan (
    id BIGSERIAL PRIMARY KEY,
    day DATE NOT NULL UNIQUE,
    prepared BOOLEAN NOT NULL DEFAULT FALSE
);
ALTER TABLE meal_plan ADD COLUMN IF NOT EXISTS prepared BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE IF NOT EXISTS meal_plan_item (
    plan_id BIGINT REFERENCES meal_plan,
    recipe_id BIGINT REFERENCES recipe,
    portions INTEGER NOT NULL CHECK (portions > 0),
    PRIMARY KEY (plan_id, recipe_id)
);
