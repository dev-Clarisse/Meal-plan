UPDATE recipes
SET title = 'Omelette nature',
    instructions = 'Battre les oeufs, faire fondre le beurre et cuire 3 minutes.'
WHERE title = 'Omelette';

UPDATE recipes
SET title = 'Gâteau moelleux',
    instructions = 'Tout mélanger et cuire au four à 180 degrés pendant 40 minutes.'
WHERE title = 'Cake';
