CREATE TABLE meal_plans (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    week_start DATE NOT NULL,
    CONSTRAINT uq_meal_plans_user_week UNIQUE (user_id, week_start)
);

CREATE TABLE meals (
    id           BIGSERIAL PRIMARY KEY,
    meal_plan_id BIGINT      NOT NULL REFERENCES meal_plans (id) ON DELETE CASCADE,
    recipe_id    BIGINT      NOT NULL REFERENCES recipes (id) ON DELETE CASCADE,
    day_of_week  VARCHAR(10) NOT NULL,
    meal_type    VARCHAR(10) NOT NULL,
    servings     INTEGER     NOT NULL CHECK (servings > 0)
);

CREATE INDEX idx_meals_plan ON meals (meal_plan_id);

CREATE TABLE shopping_lists (
    id           BIGSERIAL PRIMARY KEY,
    meal_plan_id BIGINT NOT NULL UNIQUE REFERENCES meal_plans (id) ON DELETE CASCADE,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE shopping_items (
    id               BIGSERIAL PRIMARY KEY,
    shopping_list_id BIGINT         NOT NULL REFERENCES shopping_lists (id) ON DELETE CASCADE,
    ingredient_id    BIGINT         NOT NULL REFERENCES ingredients (id),
    ingredient_name  VARCHAR(255)   NOT NULL,
    quantity         NUMERIC(12, 3) NOT NULL,
    unit             VARCHAR(255)   NOT NULL,
    purchased        BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_shopping_items_list ON shopping_items (shopping_list_id);