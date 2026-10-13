CREATE TABLE ingredients (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    default_unit VARCHAR(10)  NOT NULL
);
