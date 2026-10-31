---
name: add-front-page
description: Adds a new page protected by authentication to the Angular front of the Mijoté Meal Planner (feature folder, route with authGuard, navbar entry). Use when the user asks to create, add or scaffold a page, a screen or a menu entry in front-skeleton. Do not use for backend code, for editing an existing page, to only add a navbar link to an existing page, for public pages without authentication, or for other repositories.
---

## Procedure

1. Run `git branch --show-current` and `git status --short`. Stop and ask for a feature branch if the branch is `main`, `master`, `develop` or empty (detached HEAD). If there are uncommitted changes, continue only after the user confirms.
2. Get the page name from the request and ask only if it is missing. The technical name is English, lowercase, without accents, with hyphens (`Liste de courses` gives `shopping-list`). The route path is the technical name unless the user gives another one. The navbar label is the label explicitly given by the user, otherwise the French name of the page with a capital first letter.
3. Stop and tell the user if any of these exists: the folder `front-skeleton/src/app/features/<name>/`, a route with the same path in `front-skeleton/src/app/app.routes.ts`, or a `NAV_ITEMS` entry with the same path or the same label, including its English or French equivalent.
4. Inside `front-skeleton`, run `npm ci` if `node_modules` is missing, then `npm run build`. Note the result and any errors: this is the reference build.
5. Using `front-skeleton/src/app/features/recipes/recipe-list/` as the model for imports and shape, create `<name>.component.ts`, `.html`, `.scss` and `.spec.ts` in `front-skeleton/src/app/features/<name>/`. The component is standalone, named `<PascalName>Component`, with the selector `app-<name>`. It contains a title and a short placeholder text, no business logic, and signals only if it needs state. The spec checks that the component is created.
6. In `app.routes.ts`, add the route above the `'**'` wildcard, with the same shape as the recipes routes and `canActivate: [authGuard]`.
7. Append `{ label: '<Label>', path: '/<path>' }` at the end of `NAV_ITEMS` in `front-skeleton/src/app/shared/navbar/nav-items.ts`, following the shape of the existing entries.
8. Run `npm run build`. Fix only new errors in files created or changed by this skill, then rebuild once. Report errors already present in the reference build without touching them.
9. Do not commit. List the files created and changed, and say whether the page is verified by a passing build or not verified. The spec is not run.

## Rules

- No comments in the code.
- Texts shown to the user are in French.
- Style only with the theme variables `--olive`, `--terracotta`, `--line`, `--surface`, `--ink`, `--muted`, `--radius` and `--shadow`. Never hard-code a color.
- The only existing files this skill may edit are `app.routes.ts` and `nav-items.ts`.

## Error handling

- If the model folder `recipe-list` is missing, ask the user which existing page to copy.
- If the wildcard route `'**'` is not found, ask the user where to insert the new route.
- If the reference build fails, continue and mark the page as not verified.
- If new errors in files created or changed by this skill remain after one rebuild, stop, report the error message, keep the files and list them.
