INSERT INTO users (id, email, password_hash, role, consent_given_at, created_at, updated_at, version) 
VALUES 
(
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 
    'alice@example.com', 
    '$2a$10$7XbX8Z6...fakeHashForTest1', 
    'USER', 
    CURRENT_TIMESTAMP - INTERVAL '10' DAY, 
    CURRENT_TIMESTAMP - INTERVAL '10' DAY, 
    CURRENT_TIMESTAMP - INTERVAL '10' DAY, 
    0
),
(
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 
    'bob@example.com', 
    '$2a$10$7XbX8Z6...fakeHashForTest2', 
    'USER', 
    CURRENT_TIMESTAMP - INTERVAL '5' DAY, 
    CURRENT_TIMESTAMP - INTERVAL '5' DAY, 
    CURRENT_TIMESTAMP - INTERVAL '5' DAY, 
    0
);

INSERT INTO inventory (id, user_id, name, quantity, unit, expiry_date, created_at, updated_at, version) 
VALUES 
(
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', -- CORRIGÉ : pointe vers l'ID d'Alice (a11)
    'Lait demi-écrémé', 
    2.000, 
    'LITRE', 
    CURRENT_DATE + INTERVAL '3' DAY, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    0
),
(
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', 
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 
    'Riz basmati', 
    1.500, 
    'KG', 
    CURRENT_DATE + INTERVAL '180' DAY, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    0
),
(
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 
    'Oeufs bio', 
    6.000, 
    'UNIT', 
    CURRENT_DATE + INTERVAL '1' day, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    0
);


INSERT INTO notifications (id, user_id, inventory_id, type, message, is_read, created_at) 
VALUES 
(
    'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 
    'EXPIRY_WARNING', 
    'Votre article "Lait demi-écrémé" expire dans 3 jours.', 
    FALSE, 
    CURRENT_TIMESTAMP
),
(
    'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 
    'EXPIRY_WARNING', 
    'Votre article "Oeufs bio" expire demain !', 
    TRUE, 
    CURRENT_TIMESTAMP
);