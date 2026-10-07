-- Remove the original e-shop example tables.
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS item;

CREATE TABLE IF NOT EXISTS ingredient_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ingredient (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES ingredient_category(id),
    name VARCHAR(150) NOT NULL UNIQUE,
    default_unit VARCHAR(20) NOT NULL
);

-- One ingredient can be stored in multiple batches with different expirations.
CREATE TABLE IF NOT EXISTS stock_batch (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL REFERENCES ingredient(id),
    quantity DECIMAL(12, 3) NOT NULL CHECK (quantity >= 0),
    expiration_date DATE,
    received_date DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS recipe (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    instructions TEXT NOT NULL,
    preparation_time_minutes INTEGER CHECK (preparation_time_minutes > 0),
    servings INTEGER NOT NULL CHECK (servings > 0)
);

-- Many-to-many relationship between recipes and ingredients.
CREATE TABLE IF NOT EXISTS recipe_ingredient (
    recipe_id BIGINT NOT NULL REFERENCES recipe(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredient(id),
    quantity DECIMAL(12, 3) NOT NULL CHECK (quantity > 0),
    unit VARCHAR(20) NOT NULL,
    optional BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (recipe_id, ingredient_id)
);

CREATE INDEX IF NOT EXISTS idx_stock_batch_ingredient
    ON stock_batch (ingredient_id);

CREATE INDEX IF NOT EXISTS idx_stock_batch_expiration
    ON stock_batch (expiration_date);
