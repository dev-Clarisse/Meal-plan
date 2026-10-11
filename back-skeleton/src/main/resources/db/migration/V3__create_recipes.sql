CREATE TABLE recipes (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    servings     INTEGER      NOT NULL CHECK (servings > 0),
    instructions TEXT
);

CREATE TABLE recipe_ingredients (
    id            BIGSERIAL PRIMARY KEY,
    recipe_id     BIGINT         NOT NULL REFERENCES recipes (id) ON DELETE CASCADE,
    ingredient_id BIGINT         NOT NULL REFERENCES ingredients (id),
    quantity      NUMERIC(12, 4) NOT NULL CHECK (quantity > 0),
    unit          VARCHAR(10)    NOT NULL
);

CREATE INDEX idx_recipe_ingredients_recipe ON recipe_ingredients (recipe_id);
