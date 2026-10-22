INSERT INTO ingredients (name, default_unit)
SELECT v.ingredient_name, v.ingredient_unit
FROM (VALUES
    ('Flour',     'G'),
    ('Sugar',     'G'),
    ('Salt',      'G'),
    ('Rice',      'G'),
    ('Cheese',    'G'),
    ('Onion',     'PIECE'),
    ('Garlic',    'PIECE'),
    ('Potato',    'PIECE'),
    ('Carrot',    'PIECE'),
    ('Lemon',     'PIECE'),
    ('Olive oil', 'ML'),
    ('Cream',     'ML'),
    ('Water',     'ML'),
    ('Yeast',     'G'),
    ('Basil',     'G')
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
    ('Crêpes',               'Flour',     250,  'G'),
    ('Crêpes',               'Milk',      500,  'ML'),
    ('Crêpes',               'Egg',       3,    'PIECE'),
    ('Crêpes',               'Butter',    30,   'G'),
    ('Pâtes tomate basilic', 'Pasta',     200,  'G'),
    ('Pâtes tomate basilic', 'Tomato',    4,    'PIECE'),
    ('Pâtes tomate basilic', 'Basil',     10,   'G'),
    ('Pâtes tomate basilic', 'Olive oil', 20,   'ML'),
    ('Pâtes tomate basilic', 'Garlic',    1,    'PIECE'),
    ('Gratin dauphinois',    'Potato',    6,    'PIECE'),
    ('Gratin dauphinois',    'Cream',     300,  'ML'),
    ('Gratin dauphinois',    'Cheese',    100,  'G'),
    ('Gratin dauphinois',    'Garlic',    1,    'PIECE'),
    ('Gratin dauphinois',    'Butter',    20,   'G'),
    ('Soupe de carottes',    'Carrot',    6,    'PIECE'),
    ('Soupe de carottes',    'Onion',     1,    'PIECE'),
    ('Soupe de carottes',    'Water',     800,  'ML'),
    ('Soupe de carottes',    'Cream',     100,  'ML'),
    ('Soupe de carottes',    'Salt',      5,    'G'),
    ('Riz au lait',          'Rice',      150,  'G'),
    ('Riz au lait',          'Milk',      1000, 'ML'),
    ('Riz au lait',          'Sugar',     80,   'G'),
    ('Tarte au citron',      'Flour',     200,  'G'),
    ('Tarte au citron',      'Butter',    100,  'G'),
    ('Tarte au citron',      'Lemon',     3,    'PIECE'),
    ('Tarte au citron',      'Sugar',     120,  'G'),
    ('Tarte au citron',      'Egg',       3,    'PIECE'),
    ('Pain maison',          'Flour',     500,  'G'),
    ('Pain maison',          'Water',     320,  'ML'),
    ('Pain maison',          'Yeast',     7,    'G'),
    ('Pain maison',          'Salt',      10,   'G'),
    ('Omelette au fromage',  'Egg',       4,    'PIECE'),
    ('Omelette au fromage',  'Cheese',    60,   'G'),
    ('Omelette au fromage',  'Butter',    10,   'G'),
    ('Poulet citron',        'Chicken',   800,  'G'),
    ('Poulet citron',        'Lemon',     2,    'PIECE'),
    ('Poulet citron',        'Garlic',    3,    'PIECE'),
    ('Poulet citron',        'Olive oil', 30,   'ML'),
    ('Purée maison',         'Potato',    8,    'PIECE'),
    ('Purée maison',         'Butter',    50,   'G'),
    ('Purée maison',         'Milk',      150,  'ML'),
    ('Purée maison',         'Salt',      5,    'G'),
    ('Pâtes au poulet',      'Pasta',     300,  'G'),
    ('Pâtes au poulet',      'Chicken',   400,  'G'),
    ('Pâtes au poulet',      'Cream',     200,  'ML'),
    ('Pâtes au poulet',      'Onion',     1,    'PIECE'),
    ('Salade de tomates',    'Tomato',    4,    'PIECE'),
    ('Salade de tomates',    'Onion',     1,    'PIECE'),
    ('Salade de tomates',    'Olive oil', 20,   'ML'),
    ('Salade de tomates',    'Basil',     5,    'G')
) AS v(recipe_title, ingredient_name, quantity, unit)
JOIN recipes r ON r.title = v.recipe_title
JOIN ingredients i ON i.name = v.ingredient_name
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients ri WHERE ri.recipe_id = r.id);
