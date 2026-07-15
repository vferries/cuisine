# Somme multi-unités (g/kg, ml/l) — design

Date : 2026-07-15
Statut : validé par l'owner

## Problème

L'agrégation des ingrédients dans `tooling/src/parser.ts` ne somme les
quantités dupliquées que si l'unité est strictement identique. `@porc{500%g}`
plus `@porc{1%kg}` produit « 500 g » dans la liste d'ingrédients au lieu de
« 1,5 kg ». Limitation documentée dans AGENTS.md, à lever.

## Décisions

- **Familles convertibles** : masse `{g: 1, kg: 1000}` et volume
  `{ml: 1, l: 1000}` uniquement. Les autres unités autorisées (càc, sachet,
  gousse…) n'ont pas de conversion et gardent le comportement actuel.
- **Normalisation au parse, humanisation à l'affichage** (approche retenue) :
  le parser somme en unité de base (g, ml) ; les couches d'affichage
  reconvertissent en kg/l dès 1000. La règle d'affichage s'applique donc
  aussi au scaling live des portions (web et Android), choix explicite de
  l'owner : « 800 g » ×2 s'affiche « 1,6 kg ».
- **Humanisation vers le haut uniquement** : ≥ 1000 g → kg, ≥ 1000 ml → l.
  Jamais de conversion vers le bas à l'affichage ; conséquence assumée :
  « 0,5 l » écrit dans une recette s'affiche « 500 ml » dans la liste
  agrégée (normalisé au parse), mais reste « 0,5 l » inline dans l'étape
  tant qu'on ne scale pas.
- **Arrondi** : division par 1000 arrondie à 3 décimales max pour éviter les
  artefacts flottants. Rendu numérique inchangé par ailleurs (séparateur
  point, zéros de fin retirés par le rendu JS/Kotlin par défaut).

## Composants

### 1. Parser (`tooling/src/parser.ts`)

Dans la boucle d'agrégation : si la quantité est numérique et l'unité
appartient à une famille, normaliser (qty × facteur, unité = base) avant
insertion dans `ingredientMap`. La somme existante à unité identique fait le
reste. Quantités texte, sans unité, ou unités hors famille : inchangé
(première occurrence gardée telle quelle). Seule la liste agrégée
`ingredients` est touchée — les tokens des étapes gardent l'unité de
l'auteur. Le validator ne change pas (g/kg/ml/l déjà autorisés).

### 2. Affichage web (`web/src/lib/format.ts`)

`formatQty` humanise avant rendu : unité `g` (resp. `ml`) et quantité
numérique ≥ 1000 → qty/1000 en `kg` (resp. `l`). Couvre d'un coup la
sidebar, les quantités inline des étapes, le mode cuisson et le re-scaling
live, tous déjà branchés sur `formatQty`.

### 3. Affichage Android (`android …/data/Format.kt`)

Même règle dans le formatter Kotlin existant, testée dans `FormatTest.kt`.
Miroir du pattern `lib/random.ts` / `data/Random.kt`.

## Tests

Pas de Playwright pour cette feature : un e2e exigerait une vraie recette
contenant une duplication multi-unités, et `recipes/` est du contenu owner
(règle : ne jamais inventer de recette). La boucle externe du double-loop
est le test d'intégration du parser (source `.cook` inline → liste agrégée
en unité de base), rouge d'abord ; puis unit rouge-vert-refactor sur
`format.ts` et `Format.kt`.

Cas à couvrir :

- 500 g + 1 kg → 1500 g agrégé → affiché « 1.5 kg ».
- 200 ml + 1 l → 1200 ml → « 1.2 l ».
- 1 kg seul → normalisé 1000 g → réaffiché « 1 kg » (aller-retour stable).
- 999 g reste « 999 g » ; 1000 g devient « 1 kg ».
- Unités non convertibles dupliquées (2 gousse + 1 gousse) : somme identique
  actuelle inchangée.
- Mélange famille/hors-famille (1 kg + 1 sachet) : première occurrence
  gardée, pas de somme (comportement actuel).
- Quantité texte (« un peu ») : inchangé.
- Arrondi : 1125 g → « 1.125 kg », pas d'artefact flottant.

## Découpage en commits

1. `feat(parser)` : normalisation + somme multi-unités (tests tooling).
2. `feat(web)` : humanisation dans `formatQty` (tests Vitest).
3. `feat(android)` : humanisation dans `Format.kt` (tests JUnit).
4. `docs` : AGENTS.md — lever la limitation, cocher l'idée.
