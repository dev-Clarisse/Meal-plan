# Création d'une branche develop pour ne pas affecter la branch principale main

## Commandes à faire "immédiatement" en ouvrant le projet pour être à jour 

- git switch develop
- git fetch origin
- git pull origin develop
- git switch nom_de_votre_branche_de_travail_perso
- git rebase develop

## Commandes pour envoyer les commits de develop local à main distant

- git checkout main
- git pull origin main
- git merge develop
- git push origin main

## Commandes pour envoyer les commits de ma branche de travail à develop distant

- git checkout develop
- git pull origin develop
- git merge ma-branche-de-travail
- git push origin develop

# Squash de commits

- git rebase -i HEAD~chiffre-aux-choix
- écrire "squash" devant les commits voulus
- git push --force


## Commandes pour récupérer les commits de la branche distante main et les appliquer sur votre branche locale develop
- git checkout develop
- git fetch origin
- git rebase origin/main OU git pull origin main

## Commandes pour récupérer les commits de develop local (en aillant récupére les commits de develop distant) sur la branche de travail

- git checkout nom-de-ta-branche
- git rebase origin/develop

# Pour créer et basculer sur une nouvelle branche de travail à partir de la branche develop

- git switch -c nom-de-ta-branche
- git push -u origin nom-de-ta-branche (pour créer la branche à distance pour le permier commit)

# Supprimer une branche en local

- git branch -d nom-de-la-branche

# supprimer une branche distante 

- git push origin --delete nom-de-la-branche

# Résolution conflits

- git switch develop
- git fetch origin
- git pull origin develop

- git switch nom_de_votre_branche_de_travail_perso
- git rebase origin/develop
- on résous les conflits, puis git add .
- git rebase --continue
- git push origin nom_de_votre_branche_de_travail_perso --force-with-lease

