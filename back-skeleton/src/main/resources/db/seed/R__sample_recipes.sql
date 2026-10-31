INSERT INTO ingredients (name, default_unit)
SELECT v.ingredient_name, v.ingredient_unit
FROM (VALUES
    ('Farine',     'G'),
    ('Sucre',     'G'),
    ('Sel',      'G'),
    ('Riz',      'G'),
    ('Fromage',    'G'),
    ('Oignon',     'PIECE'),
    ('Ail',    'PIECE'),
    ('Pomme de terre',    'PIECE'),
    ('Carotte',    'PIECE'),
    ('Citron',     'PIECE'),
    ('Huile d''olive', 'ML'),
    ('Crème',     'ML'),
    ('Eau',     'ML'),
    ('Levure',     'G'),
    ('Basilic',     'G')
) AS v(ingredient_name, ingredient_unit)
WHERE NOT EXISTS (SELECT 1 FROM ingredients i WHERE i.name = v.ingredient_name);

INSERT INTO recipes (title, servings, instructions)
SELECT v.recipe_title, v.recipe_servings, v.recipe_instructions
FROM (VALUES
    ('Crêpes', 4, 'Mélanger, laisser reposer 1 h, cuire à la poêle.'),
    ('Pâtes tomate basilic', 2, 'Cuire les pâtes, préparer la sauce, mélanger.'),
    ('Gratin dauphinois', 4, 'Couper en rondelles, couvrir de crème, 1 h au four.'),
    ('Soupe de carottes', 4, 'Faire revenir, couvrir d''eau, mixer.'),
    ('Riz au lait', 4, 'Cuire doucement le riz dans le lait sucré.'),
    ('Tarte au citron', 6, 'Cuire la pâte à blanc, ajouter la crème citron.'),
    ('Pain maison', 8, 'Pétrir, laisser lever 2 h, cuire 35 min.'),
    ('Omelette au fromage', 2, 'Battre les oeufs, ajouter le fromage, cuire.'),
    ('Poulet citron', 4, 'Mariner puis rôtir au four.'),
    ('Purée maison', 4, 'Cuire les pommes de terre, écraser avec le beurre.'),
    ('Pâtes au poulet', 3, 'Saisir le poulet, ajouter la crème et les pâtes.'),
    ('Salade de tomates', 2, 'Couper, assaisonner, laisser reposer.')
) AS v(recipe_title, recipe_servings, recipe_instructions)
WHERE NOT EXISTS (SELECT 1 FROM recipes r WHERE r.title = v.recipe_title);

INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit)
SELECT r.id, i.id, v.quantity, v.unit
FROM (VALUES
    ('Crêpes',               'Farine',     250,  'G'),
    ('Crêpes',               'Lait',      500,  'ML'),
    ('Crêpes',               'Oeuf',       3,    'PIECE'),
    ('Crêpes',               'Beurre',    30,   'G'),
    ('Pâtes tomate basilic', 'Pâtes',     200,  'G'),
    ('Pâtes tomate basilic', 'Tomate',    4,    'PIECE'),
    ('Pâtes tomate basilic', 'Basilic',     10,   'G'),
    ('Pâtes tomate basilic', 'Huile d''olive', 20,   'ML'),
    ('Pâtes tomate basilic', 'Ail',    1,    'PIECE'),
    ('Gratin dauphinois',    'Pomme de terre',    6,    'PIECE'),
    ('Gratin dauphinois',    'Crème',     300,  'ML'),
    ('Gratin dauphinois',    'Fromage',    100,  'G'),
    ('Gratin dauphinois',    'Ail',    1,    'PIECE'),
    ('Gratin dauphinois',    'Beurre',    20,   'G'),
    ('Soupe de carottes',    'Carotte',    6,    'PIECE'),
    ('Soupe de carottes',    'Oignon',     1,    'PIECE'),
    ('Soupe de carottes',    'Eau',     800,  'ML'),
    ('Soupe de carottes',    'Crème',     100,  'ML'),
    ('Soupe de carottes',    'Sel',      5,    'G'),
    ('Riz au lait',          'Riz',      150,  'G'),
    ('Riz au lait',          'Lait',      1000, 'ML'),
    ('Riz au lait',          'Sucre',     80,   'G'),
    ('Tarte au citron',      'Farine',     200,  'G'),
    ('Tarte au citron',      'Beurre',    100,  'G'),
    ('Tarte au citron',      'Citron',     3,    'PIECE'),
    ('Tarte au citron',      'Sucre',     120,  'G'),
    ('Tarte au citron',      'Oeuf',       3,    'PIECE'),
    ('Pain maison',          'Farine',     500,  'G'),
    ('Pain maison',          'Eau',     320,  'ML'),
    ('Pain maison',          'Levure',     7,    'G'),
    ('Pain maison',          'Sel',      10,   'G'),
    ('Omelette au fromage',  'Oeuf',       4,    'PIECE'),
    ('Omelette au fromage',  'Fromage',    60,   'G'),
    ('Omelette au fromage',  'Beurre',    10,   'G'),
    ('Poulet citron',        'Poulet',   800,  'G'),
    ('Poulet citron',        'Citron',     2,    'PIECE'),
    ('Poulet citron',        'Ail',    3,    'PIECE'),
    ('Poulet citron',        'Huile d''olive', 30,   'ML'),
    ('Purée maison',         'Pomme de terre',    8,    'PIECE'),
    ('Purée maison',         'Beurre',    50,   'G'),
    ('Purée maison',         'Lait',      150,  'ML'),
    ('Purée maison',         'Sel',      5,    'G'),
    ('Pâtes au poulet',      'Pâtes',     300,  'G'),
    ('Pâtes au poulet',      'Poulet',   400,  'G'),
    ('Pâtes au poulet',      'Crème',     200,  'ML'),
    ('Pâtes au poulet',      'Oignon',     1,    'PIECE'),
    ('Salade de tomates',    'Tomate',    4,    'PIECE'),
    ('Salade de tomates',    'Oignon',     1,    'PIECE'),
    ('Salade de tomates',    'Huile d''olive', 20,   'ML'),
    ('Salade de tomates',    'Basilic',     5,    'G')
) AS v(recipe_title, ingredient_name, quantity, unit)
JOIN recipes r ON r.title = v.recipe_title
JOIN ingredients i ON i.name = v.ingredient_name
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = r.id);
