# Chip « De saison » — design

Date : 2026-07-15
Statut : validé par l'owner

## Problème

Aucun moyen de filtrer l'accueil sur « qu'est-ce qui se cuisine en ce
moment ? ». On ajoute une metadata `saison:` aux `.cook` et une chip
« De saison » (web + Android) qui compare au mois courant.

## Décisions

- **Format** : `>> saison: toute l'année` ou `>> saison: <mois>-<mois>`
  (noms de mois français en minuscules, ex. `octobre-mars` ; la plage peut
  enjamber l'année ; `mai-mai` = un seul mois). Une seule plage par recette.
- **Metadata requise** : le validator la traite comme `course` — erreur si
  absente ou mal formée. Conséquence : backfill des 15 recettes existantes
  AVANT d'activer l'erreur, sinon CI rouge.
- **Expansion au build** (approche retenue) : `build-index` parse la valeur
  une seule fois et expose `saisonMonths: number[]` (1-12) dans l'index
  (`octobre-mars` → `[10,11,12,1,2,3]`, `toute l'année` → les 12). Les
  prédicats web/Android testent `moisCourant ∈ saisonMonths` — aucune
  logique de plage dupliquée.
- **Mois courant injecté** partout (testabilité, pattern `pickRandom`) :
  jamais de `new Date()` dans une fonction testée.
- **Backfill = contenu owner** : les valeurs proposées par Claude sont
  validées une à une par l'owner avant commit. Jamais de saison inventée
  committée sans accord.

## Composants

### 1. Validator (`tooling/src/validate-cook.ts`)

`saison` rejoint les metadata requises (erreur si absente). Valeur valide :
`toute l'année` littéral, ou `^<mois>-<mois>$` avec les 12 noms français en
minuscules (janvier…décembre). Erreur sinon, avec message qui rappelle le
format. Activé dans le même commit que le backfill.

### 2. Build (`tooling/src/build-index.ts`)

Fonction pure exportée `expandSaison(value: string): number[]` :
- `"toute l'année"` → `[1..12]` ;
- `"octobre-mars"` → `[10,11,12,1,2,3]` (wrap si début > fin) ;
- `"mai-mai"` → `[5]`.
Chaque entrée de `index.json` gagne `saisonMonths: number[]`.

### 3. Web (`web/src/lib/chips.ts`, `web/src/pages/index.astro`)

- `ChipFilterDoc` gagne `saisonMonths: number[]`.
- `ChipKey` gagne `"saison"` ; `FilterContext` gagne `month?: number`
  (1-12). Prédicat : `r.saisonMonths.includes(ctx.month)`.
- `index.astro` : bouton chip « De saison » après « Rapide », passe
  `new Date().getMonth() + 1` dans le contexte au moment du filtrage.

### 4. Android (`data/`, `ui/`)

- `RecipeMeta` + entité Room : champ `saisonMonths` (liste d'entiers,
  converter existant ou ajout). Bump de version Room avec migration
  destructive — c'est un cache, il se re-fetch depuis Pages.
- `ChipKey.SAISON("De saison")` ; `filterByChip(recipes, chip, favorites,
  month)` — `month` injecté, fourni par l'appelant UI avec le mois courant.

### 5. Docs & import

- CONVENTIONS.md : documenter le format `saison:`.
- Skill `/import-recette` : la salve de questions demande la saison quand
  elle n'est pas déductible de la source (les règles restent dans
  CONVENTIONS.md, référencées).
- AGENTS.md : cocher l'idée, décrire la mécanique.

### 6. Backfill des 15 recettes

Claude propose un tableau slug → valeur suggérée (saisonnalité des
ingrédients : girolles → automne, gaspacho → été, etc.), l'owner corrige et
valide chaque ligne, puis un commit applique les 15 metadata + l'activation
de l'erreur validator.

## Tests

Double-loop, unit d'abord là où le contenu réel ne peut pas être fixé :

- `expandSaison` : toute l'année, plage simple, wrap, mois unique.
- Validator : absente, format invalide, mois inconnu, valides.
- `chips.ts` : chip saison avec mois injecté (dans/hors plage), AND avec
  les autres filtres inchangé.
- `Chips.kt` : mêmes cas côté Kotlin.
- E2e Playwright : la chip « De saison » existe, est cliquable, et le
  compteur de résultats reste cohérent (pas d'assertion sur un contenu
  précis — le résultat dépend du mois d'exécution et du contenu owner).

## Découpage en commits

1. `feat(tooling)` : validator (désactivé pour `saison` manquante — warning
   provisoire) + `expandSaison` + `saisonMonths` dans l'index.
2. `feat(web)` : chip De saison + prédicat + e2e.
3. `feat(android)` : RecipeMeta/Room + chip + month injecté.
4. `feat(recipes)` : backfill validé par l'owner + passage du warning en
   erreur.
5. `docs` : CONVENTIONS.md, import-recette, AGENTS.md.
