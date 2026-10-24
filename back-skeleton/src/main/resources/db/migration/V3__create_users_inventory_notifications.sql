CREATE TABLE users (
    id               UUID         PRIMARY KEY,
    email            VARCHAR(254) NOT NULL,
    password_hash    VARCHAR(72)  NOT NULL,
    role             VARCHAR(20)  NOT NULL,
    consent_given_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    version          BIGINT,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE inventory (
    id          UUID           PRIMARY KEY,
    user_id     UUID           NOT NULL REFERENCES users (id),
    name        VARCHAR(120)   NOT NULL,
    quantity    NUMERIC(12, 3) NOT NULL,
    unit        VARCHAR(20)    NOT NULL,
    expiry_date DATE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    version     BIGINT
);

CREATE INDEX idx_inventory_user   ON inventory (user_id);
CREATE INDEX idx_inventory_expiry ON inventory (expiry_date);

CREATE TABLE notifications (
    id           UUID         PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES users (id),
    inventory_id UUID         NOT NULL REFERENCES inventory (id),
    type         VARCHAR(30)  NOT NULL,
    message      VARCHAR(500) NOT NULL,
    is_read      BOOLEAN      NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_notif_inventory_type UNIQUE (inventory_id, type)
);

CREATE INDEX idx_notif_user ON notifications (user_id);
