INSERT INTO recipes (title, servings, instructions) VALUES
    ('Omelette', 2, 'Beat the eggs, melt the butter and cook for 3 minutes.'),
    ('Cake', 6, 'Mix everything and bake at 180C for 40 minutes.');

INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit)
SELECT r.id, i.id, v.quantity, v.unit
FROM (VALUES
    ('Omelette', 'Egg',    4,  'PIECE'),
    ('Omelette', 'Butter', 20, 'G'),
    ('Cake',     'Egg',    4,  'PIECE'),
    ('Cake',     'Butter', 100, 'G'),
    ('Cake',     'Milk',   0.25, 'L')
) AS v(recipe_title, ingredient_name, quantity, unit)
JOIN recipes r ON r.title = v.recipe_title
JOIN ingredients i ON i.name = v.ingredient_name;
