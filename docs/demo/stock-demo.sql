-- Secours pour la démo : remplit le stock du scénario des œufs pour un utilisateur.
-- À utiliser seulement si la page Inventaire ne permet pas d'ajouter les aliments.
-- Usage (PowerShell) :
--   Get-Content docs\demo\stock-demo.sql | docker exec -i <conteneur> psql -U <utilisateur> -d <base> -v "email='demo@mealplanner.fr'"
INSERT INTO inventory (id, user_id, name, quantity, unit, expiry_date, created_at, updated_at, version)
SELECT gen_random_uuid(), u.id, v.name, v.quantity, v.unit, v.expiry, now(), now(), 0
FROM users u
CROSS JOIN (VALUES
    ('Oeuf',    6,   'PIECE', current_date + 2),
    ('Beurre',  200, 'GRAM',  current_date + 30),
    ('Lait',    1,   'LITER', current_date + 7),
    ('Poulet',  500, 'GRAM',  current_date + 3)
) AS v(name, quantity, unit, expiry)
WHERE u.email = :email;
