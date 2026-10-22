# Script de la démo finale (5 à 7 minutes)

Fil rouge : **les œufs**. Les recettes demandent 8 œufs, le frigo en contient 6, l'application calcule seule qu'il faut en acheter 2.

| Ordre | Qui | Durée | Ce qu'on montre |
|---|---|---|---|
| 1 | Clarisse | 1 min 30 | Connexion, inventaire (6 œufs, 500 g de poulet), badge orange |
| 2 | Leticia | 1 min 30 | Création d'une recette avec le formulaire dynamique, recherche |
| 3 | Georgette | 2 min 30 | Planning, glisser-déposer, manquants, liste de courses : **2 œufs** |
| 4 | Clarisse puis toutes | 1 min | Cloche des alertes, conclusion, questions |

---

## La veille de la démo

Les alertes de péremption sont créées **une fois par jour, à 7 h**. Pour que la cloche montre l'alerte des œufs, le stock doit exister **avant 7 h le jour de la démo** et le backend doit tourner à ce moment-là.

1. Créer le compte de démo sur `/register` : `demo@mealplanner.fr` / `demo-mealplanner-2026`.
2. Remplir le stock du compte de démo :
   - **normalement** depuis la page Inventaire (c'est le passage de Clarisse) ;
   - **en secours**, si la page Inventaire ne fonctionne pas, avec le script SQL :

     ```powershell
     Get-Content docs\demo\stock-demo.sql | docker exec -i <conteneur_postgres> psql -U <utilisateur> -d <base> -v "email='demo@mealplanner.fr'"
     ```

     La commande doit afficher `INSERT 0 4`.
   - Les œufs doivent expirer **dans les 3 jours qui suivent la démo** (badge orange). Le script SQL les fait expirer 2 jours après la date où il est lancé.

## Juste avant la démo (15 minutes avant)

1. Lancer la base, le backend et le front (voir le README).
   - Backend : http://localhost:8085 — Front : http://localhost:4200
2. Vérifier que le **planning de la semaine en cours est vide** (sinon prendre la semaine suivante avec la flèche).
3. Vérifier que la cloche affiche bien l'alerte des œufs.
4. Ouvrir un onglet par passage : Inventaire, Recettes, Planning. Zoom du navigateur à 125 % pour le vidéoprojecteur.
5. Se reconnecter juste avant de commencer : **la session expire au bout de 15 minutes**.

---

## 1. Clarisse : ce qu'il y a dans le frigo

- Se connecter avec le compte de démo.
- Montrer l'inventaire : 6 œufs (expirent dans 2 jours), 500 g de poulet, beurre, lait.
- Pointer le **badge orange** sur les œufs : « ils expirent bientôt, l'application le voit ».

> Phrase de transition : « On sait ce qu'on a. Maintenant, qu'est-ce qu'on veut cuisiner ? »

## 2. Leticia : les recettes

- Créer la recette **« Salade de tomates »** avec le formulaire dynamique (ajouter puis retirer une ligne d'ingrédient).
- Taper « tom » dans la recherche : « Salade de tomates » apparaît.
- Ouvrir **Omelette** (4 œufs pour 2 portions) et **Cake** (4 œufs pour 6 portions) : ce sont elles qu'on va planifier.

> Phrase de transition : « Les recettes sont en grammes, en litres, en pièces : c'est le système d'unités qui permet de les comparer au stock. »

## 3. Georgette : le planning et la liste de courses

**Ouvrir la page Planning.**

1. « Voici la semaine : 7 jours, 3 repas par jour. »
2. Ajouter **Omelette**, **2 portions**, **mardi**, **déjeuner**.
3. Ajouter **Cake**, **6 portions**, **mercredi**, **dîner**.
4. **Glisser l'omelette du mardi au jeudi.**
   « Le déplacement est enregistré en base : si je recharge la page, l'omelette est toujours jeudi. » → recharger (F5) pour le prouver.
5. Cliquer sur **« Calculer les ingrédients manquants »**.
   « Il faut 4 + 4 = 8 œufs. Le frigo en a 6. Il en manque **2**. Le beurre et le lait suffisent, donc ils n'apparaissent pas. »
   → affichage attendu : **Egg : 2 pièces**, et rien d'autre.
6. Cliquer sur **« Générer la liste de courses »** → **2 œufs** apparaissent.
7. Cocher les œufs comme achetés, puis cliquer sur **« Régénérer la liste »**.
   « Si je change mon planning et que je régénère, ce qui est déjà coché est conservé, et il n'y a jamais de doublon. »

**Si une question porte sur le calcul** : on additionne d'abord les besoins de *tous* les repas, en unité de base (g, mL, pièce), puis on soustrait le stock **non périmé**. Le scénario est couvert par un test automatisé (`EggScenarioIntegrationTest`).

> Phrase de transition : « Et pendant ce temps, les œufs continuent de vieillir… »

## 4. Clarisse puis toutes : alertes et conclusion

- Clarisse ouvre la **cloche** : l'alerte de péremption des œufs est là.
- Conclusion (une phrase chacune) : ce que chacune a construit, puis questions.

---

## Si quelque chose ne marche pas pendant la démo

| Problème | Réaction |
|---|---|
| Tout se met à échouer, toasts rouges en série | La session a expiré : se reconnecter, reprendre l'étape en cours |
| Le planning de la semaine contient déjà des repas | Flèche « Semaine suivante » et refaire les ajouts |
| Les manquants affichent 8 œufs au lieu de 2 | Le stock du compte est vide : lancer le script SQL de secours, puis recalculer |
| Les manquants affichent 0 œuf | Le stock a déjà assez d'œufs (démo rejouée) : le montrer comme le cas « le stock suffit, la liste est vide » |
| Le glisser-déposer ne réagit pas | Recharger la page ; en dernier recours, montrer le déplacement déjà fait |

## Répétitions

Objectif du guide : **la démo répétée 2 fois sans accroc**.

- [ ] Répétition 1 — date : ______ — durée : ______ — problèmes :
- [ ] Répétition 2 — date : ______ — durée : ______ — problèmes :
