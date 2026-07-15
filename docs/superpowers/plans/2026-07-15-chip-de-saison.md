# Chip « De saison » Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Metadata `saison:` dans les `.cook` + chip « De saison » sur l'accueil web et Android, comparée au mois courant injecté.

**Architecture:** `tooling/src/saison.ts` (nouveau) porte `isValidSaison` + `expandSaison` ; `build-index` expose `saisonMonths: number[]` dans l'index ; les prédicats web (`chips.ts`) et Android (`Chips.kt`) testent l'appartenance du mois injecté — aucune logique de plage dupliquée. Backfill des 15 recettes validé par l'owner avant de rendre la metadata requise. Spec : `docs/superpowers/specs/2026-07-15-chip-de-saison-design.md`.

**Tech Stack:** TypeScript strict, Vitest, Playwright, Kotlin + JUnit, Room.

## Global Constraints

- Format : `toute l'année` littéral OU `<mois>-<mois>` (12 noms français en minuscules : janvier…décembre), plage pouvant enjamber l'année, `mai-mai` = un seul mois.
- Jamais de `new Date()` / `Calendar` dans une fonction testée — le mois est injecté (pattern `pickRandom`).
- `recipes/` = contenu owner : le backfill (Task 4) exige la validation de l'owner valeur par valeur AVANT tout commit ; les fixtures de test restent inline.
- Ordre strict : la metadata ne devient requise (erreur validator) qu'au commit du backfill (Task 4) — avant, absence = warning.
- Commits courts (sujet + 2-4 lignes de corps max), pas de Co-Authored-By, pas de push.
- TDD : chaque test rouge doit échouer sur une assertion, pas sur un import.

---

### Task 1: Tooling — saison.ts, validator, saisonMonths dans l'index

**Files:**
- Create: `tooling/src/saison.ts`
- Create: `tooling/src/saison.test.ts`
- Modify: `tooling/src/validate-cook.ts` (imports en tête ; ajout après le bloc `meta.source`, ~ligne 92)
- Modify: `tooling/src/validate-cook.test.ts` (nouveaux cas)
- Modify: `tooling/src/build-index.ts` (interface `RecipeMeta` ~ligne 13, `metaList.push` ~ligne 103)

**Interfaces:**
- Consumes: `parseCook` existant (metadata brutes).
- Produces: `isValidSaison(value: string): boolean` et `expandSaison(value: string): number[]` exportés de `tooling/src/saison.ts` ; chaque entrée d'`index.json` gagne `saisonMonths: number[]` (vide si metadata absente — état transitoire jusqu'à la Task 4). Les Tasks 2-3 consomment `saisonMonths`.

- [ ] **Step 1: Write the failing tests**

Créer `tooling/src/saison.test.ts` :

```ts
import { describe, expect, it } from "vitest";
import { expandSaison, isValidSaison } from "./saison";

describe("isValidSaison", () => {
  it("accepte « toute l'année » et les plages valides", () => {
    expect(isValidSaison("toute l'année")).toBe(true);
    expect(isValidSaison("octobre-mars")).toBe(true);
    expect(isValidSaison("mai-mai")).toBe(true);
  });

  it("rejette mois inconnu, casse, format", () => {
    expect(isValidSaison("octobre")).toBe(false);
    expect(isValidSaison("Octobre-Mars")).toBe(false);
    expect(isValidSaison("octobre-msra")).toBe(false);
    expect(isValidSaison("été")).toBe(false);
    expect(isValidSaison("")).toBe(false);
  });
});

describe("expandSaison", () => {
  it("toute l'année → les 12 mois", () => {
    expect(expandSaison("toute l'année")).toEqual([
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    ]);
  });

  it("plage simple juin-septembre", () => {
    expect(expandSaison("juin-septembre")).toEqual([6, 7, 8, 9]);
  });

  it("plage qui enjambe l'année octobre-mars", () => {
    expect(expandSaison("octobre-mars")).toEqual([10, 11, 12, 1, 2, 3]);
  });

  it("mois unique mai-mai", () => {
    expect(expandSaison("mai-mai")).toEqual([5]);
  });

  it("valeur invalide → liste vide", () => {
    expect(expandSaison("n'importe quoi")).toEqual([]);
  });
});
```

Ajouter à la fin de `tooling/src/validate-cook.test.ts` (réutiliser le header valide déjà présent dans le fichier pour construire la source ; sinon ce squelette) :

```ts
describe("validateRecipe — saison", () => {
  const base = [
    ">> title: Test",
    ">> servings: 4",
    ">> prep time: 10 min",
    ">> cook time: 20 min",
    ">> difficulty: facile",
    ">> cuisine: française",
    ">> course: plat",
    ">> tags: test",
    ">> source: test",
    "",
    "== Étapes ==",
    "",
    "Faire quelque chose.",
    "",
  ];

  it("warning si saison absente (provisoire, avant backfill)", () => {
    const { errors, warnings } = validateRecipe(base.join("\n"));
    expect(errors).toEqual([]);
    expect(warnings.some((w) => w.includes('"saison"'))).toBe(true);
  });

  it("erreur si saison malformée", () => {
    const src = [">> saison: printemps", ...base].join("\n");
    const { errors } = validateRecipe(src);
    expect(errors.some((e) => e.includes('"saison" invalide'))).toBe(true);
  });

  it("valide avec une plage ou toute l'année", () => {
    for (const v of ["octobre-mars", "toute l'année"]) {
      const { errors } = validateRecipe([`>> saison: ${v}`, ...base].join("\n"));
      expect(errors).toEqual([]);
    }
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `pnpm --filter @cuisine/tooling test`
Expected: FAIL — `saison.test.ts` ne compile pas encore (module absent) : créer d'abord un `saison.ts` vide exportant les deux fonctions avec `return false` / `return []` pour obtenir des échecs d'assertion, pas d'import. Les tests validator échouent sur les assertions (`warnings` vide, pas d'erreur "invalide").

- [ ] **Step 3: Write the implementation**

Créer `tooling/src/saison.ts` :

```ts
export const FRENCH_MONTHS = [
  "janvier",
  "février",
  "mars",
  "avril",
  "mai",
  "juin",
  "juillet",
  "août",
  "septembre",
  "octobre",
  "novembre",
  "décembre",
] as const;

export const ALL_YEAR = "toute l'année";

function monthIndex(name: string): number {
  return FRENCH_MONTHS.indexOf(name as (typeof FRENCH_MONTHS)[number]) + 1;
}

export function isValidSaison(value: string): boolean {
  if (value === ALL_YEAR) return true;
  const parts = value.split("-");
  if (parts.length !== 2) return false;
  return monthIndex(parts[0]) > 0 && monthIndex(parts[1]) > 0;
}

export function expandSaison(value: string): number[] {
  if (!isValidSaison(value)) return [];
  if (value === ALL_YEAR) {
    return Array.from({ length: 12 }, (_, i) => i + 1);
  }
  const [start, end] = value.split("-").map(monthIndex);
  const months: number[] = [];
  for (let m = start; ; m = (m % 12) + 1) {
    months.push(m);
    if (m === end) break;
  }
  return months;
}
```

Dans `tooling/src/validate-cook.ts` : ajouter l'import en tête :

```ts
import { isValidSaison } from "./saison.ts";
```

puis, juste après le bloc `if (!meta.source) { ... }` (~ligne 92) :

```ts
  if (!meta.saison) {
    warnings.push(
      `metadata "saison" absente — bientôt requise ("toute l'année" ou "<mois>-<mois>")`,
    );
  } else if (!isValidSaison(meta.saison)) {
    errors.push(
      `metadata "saison" invalide: "${meta.saison}" (attendu: "toute l'année" ou "<mois>-<mois>" en minuscules, ex. octobre-mars)`,
    );
  }
```

Dans `tooling/src/build-index.ts` : import en tête :

```ts
import { expandSaison } from "./saison.ts";
```

dans l'interface `RecipeMeta` (~ligne 13), après `tags: string[];` :

```ts
  saisonMonths: number[];
```

dans le `metaList.push({...})` (~ligne 103), après `tags: parseTags(meta.tags),` :

```ts
        saisonMonths: meta.saison ? expandSaison(meta.saison) : [],
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `pnpm --filter @cuisine/tooling test` puis `pnpm validate`
Expected: tests PASS ; `pnpm validate` affiche le warning « saison absente » sur les 15 recettes mais exit 0.

- [ ] **Step 5: Commit**

```bash
git add tooling/src/saison.ts tooling/src/saison.test.ts \
        tooling/src/validate-cook.ts tooling/src/validate-cook.test.ts \
        tooling/src/build-index.ts
git commit -m "feat(tooling): metadata saison, expandSaison, saisonMonths dans l'index

Warning provisoire si absente ; passera en erreur avec le backfill."
```

---

### Task 2: Web — chip « De saison »

**Files:**
- Modify: `web/src/lib/chips.ts`
- Modify: `web/src/lib/chips.test.ts`
- Modify: `web/src/pages/index.astro` (nav chips ~ligne 50, `render()` ~ligne 289)
- Create: `web/e2e/saison.spec.ts`

**Interfaces:**
- Consumes: `saisonMonths: number[]` dans les entrées d'`index.json` (Task 1).
- Produces: `ChipKey` inclut `"saison"` ; `FilterContext` gagne `month?: number` (1-12). `filterByChip(recipes, "saison", { month })` garde les recettes dont `saisonMonths` contient `month` ; sans `month`, ne filtre rien.

- [ ] **Step 1: Write the failing unit tests**

Ajouter à la fin de `web/src/lib/chips.test.ts` :

```ts
describe("filterByChip — saison", () => {
  const doc = (slug: string, saisonMonths: number[]): ChipFilterDoc => ({
    slug,
    cuisine: "",
    tags: [],
    totalTime: 0,
    saisonMonths,
  });

  it("garde les recettes dont la plage contient le mois injecté", () => {
    const recipes = [doc("ete", [6, 7, 8]), doc("hiver", [12, 1, 2])];
    expect(filterByChip(recipes, "saison", { month: 7 })).toEqual(["ete"]);
    expect(filterByChip(recipes, "saison", { month: 1 })).toEqual(["hiver"]);
  });

  it("exclut une recette sans saisonMonths quand un mois est fourni", () => {
    const bare: ChipFilterDoc = { slug: "x", cuisine: "", tags: [], totalTime: 0 };
    expect(filterByChip([bare], "saison", { month: 7 })).toEqual([]);
  });

  it("sans mois fourni, ne filtre rien", () => {
    const recipes = [doc("ete", [6, 7, 8])];
    expect(filterByChip(recipes, "saison", {})).toEqual(["ete"]);
  });
});
```

(Si le fichier importe déjà `ChipFilterDoc` et `filterByChip`, ne pas dupliquer les imports.)

- [ ] **Step 2: Write the failing e2e test**

Créer `web/e2e/saison.spec.ts` :

```ts
import { expect, test } from "@playwright/test";

test("la chip De saison filtre sans casser la liste", async ({ page }) => {
  await page.goto("/");

  const rows = page.locator(".recipe-row:visible");
  const total = await rows.count();
  expect(total).toBeGreaterThan(0);

  await page.getByRole("button", { name: "De saison" }).click();
  expect(await rows.count()).toBeLessThanOrEqual(total);

  await page.getByRole("button", { name: "Toutes" }).click();
  await expect(rows).toHaveCount(total);
});
```

(Pas d'assertion sur un contenu précis : le résultat dépend du mois d'exécution et du contenu owner.)

- [ ] **Step 3: Run tests to verify they fail**

Run: `pnpm --filter @cuisine/web test`
Expected: FAIL — TypeScript refuse `"saison"` comme `ChipKey` / `month` inconnu dans `FilterContext` (échec de compilation attendu ici car l'API n'existe pas encore ; les assertions prennent le relais dès que les types existent).
Run: `pnpm test:e2e -- saison.spec.ts`
Expected: FAIL — bouton « De saison » introuvable.

- [ ] **Step 4: Write the implementation**

Dans `web/src/lib/chips.ts` :

`ChipFilterDoc` gagne (après `totalTime: number;`) :

```ts
  saisonMonths?: number[];
```

`ChipKey` gagne `"saison"` (après `"rapide"`) :

```ts
export type ChipKey =
  | "all"
  | "rapide"
  | "saison"
  | "vege"
  | "asiatique"
  | "francais"
  | "dessert"
  | "favoris";
```

`FilterContext` :

```ts
export interface FilterContext {
  favorites?: string[];
  month?: number;
}
```

Dans `filterByChip`, après le bloc `favoris` :

```ts
  if (chip === "saison") {
    const { month } = ctx;
    if (month === undefined) return recipes.map((r) => r.slug);
    return recipes
      .filter((r) => (r.saisonMonths ?? []).includes(month))
      .map((r) => r.slug);
  }
```

(Le `Record` `CHIP_PREDICATES` exclut déjà `all`/`favoris` ; étendre l'exclusion : `Exclude<ChipKey, "all" | "favoris" | "saison">`.)

Dans `web/src/pages/index.astro` : ajouter le bouton après « Rapide » (~ligne 50) :

```html
      <button class="chip" type="button" data-chip="saison">De saison</button>
```

et dans `render()` (~ligne 289), passer le mois courant :

```ts
      const allowedByChip = new Set(
        filterByChip(recipes, activeChip, {
          favorites,
          month: new Date().getMonth() + 1,
        }),
      );
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `pnpm validate && pnpm test && pnpm test:e2e`
Expected: PASS partout (le e2e rebuild l'index, donc `saisonMonths` présent).

- [ ] **Step 6: Commit**

```bash
git add web/src/lib/chips.ts web/src/lib/chips.test.ts \
        web/src/pages/index.astro web/e2e/saison.spec.ts
git commit -m "feat(web): chip De saison sur l'accueil

Prédicat sur saisonMonths avec mois courant injecté via FilterContext."
```

---

### Task 3: Android — chip De saison + Room

**Files:**
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/RecipeMeta.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/Chips.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/cache/Entities.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/cache/CuisineDatabase.kt`
- Modify: `android/app/src/main/java/fr/vferries/cuisine/ui/HomeScreen.kt` (~lignes 137-150)
- Test: `android/app/src/test/java/fr/vferries/cuisine/data/ChipsTest.kt`

**Interfaces:**
- Consumes: `saisonMonths` du JSON (Task 1) — champ à défaut `emptyList()` pour les caches/JSON antérieurs.
- Produces: `ChipKey.SAISON("De saison")` ; `filterByChip(recipes: List<RecipeMeta>, chip: ChipKey, favorites: Set<String> = emptySet(), month: Int = 0): List<String>` — `month = 0` signifie « pas de filtre » (miroir du `month: undefined` web).

- [ ] **Step 1: Write the failing tests**

Dans `ChipsTest.kt`, ajouter (adapter le helper existant si le fichier en a déjà un — sinon celui-ci) :

```kotlin
    private fun saisonMeta(slug: String, saisonMonths: List<Int>) = RecipeMeta(
        slug = slug,
        title = slug,
        servings = 4,
        prepTime = 0,
        cookTime = 0,
        totalTime = 0,
        difficulty = "facile",
        cuisine = "",
        updatedAt = "",
        saisonMonths = saisonMonths,
    )

    @Test fun saison_keeps_recipes_matching_injected_month() {
        val recipes = listOf(
            saisonMeta("ete", listOf(6, 7, 8)),
            saisonMeta("hiver", listOf(12, 1, 2)),
        )
        assertEquals(listOf("ete"), filterByChip(recipes, ChipKey.SAISON, month = 7))
        assertEquals(listOf("hiver"), filterByChip(recipes, ChipKey.SAISON, month = 1))
    }

    @Test fun saison_excludes_recipes_without_months_when_month_given() {
        val recipes = listOf(saisonMeta("x", emptyList()))
        assertEquals(emptyList<String>(), filterByChip(recipes, ChipKey.SAISON, month = 7))
    }

    @Test fun saison_without_month_filters_nothing() {
        val recipes = listOf(saisonMeta("ete", listOf(6, 7, 8)))
        assertEquals(listOf("ete"), filterByChip(recipes, ChipKey.SAISON))
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd android && ./gradlew testDebugUnitTest --tests "fr.vferries.cuisine.data.ChipsTest"`
Expected: FAIL de compilation sur `ChipKey.SAISON` / `saisonMonths` / `month` (l'API n'existe pas) — créer d'abord les champs/enum/paramètre avec un corps qui renvoie tout (`return recipes.map { it.slug }` pour SAISON) si tu veux un rouge d'assertion pur, puis constater l'échec d'assertion.

- [ ] **Step 3: Write the implementation**

`RecipeMeta.kt` — après `ingredientNames` :

```kotlin
    val saisonMonths: List<Int> = emptyList(),
```

`Chips.kt` — `SAISON("De saison"),` juste après `RAPIDE("Rapide"),` dans l'enum. Dans `filterByChip` :

```kotlin
fun filterByChip(
    recipes: List<RecipeMeta>,
    chip: ChipKey,
    favorites: Set<String> = emptySet(),
    month: Int = 0,
): List<String> {
    if (chip == ChipKey.FAVORIS) {
        return recipes.filter { it.slug in favorites }.map { it.slug }
    }
    if (chip == ChipKey.SAISON) {
        if (month == 0) return recipes.map { it.slug }
        return recipes.filter { month in it.saisonMonths }.map { it.slug }
    }
    val predicate = predicates.getValue(chip)
    return recipes.filter(predicate).map { it.slug }
}
```

(Ne pas ajouter SAISON à la map `predicates` — elle n'a pas accès au mois.)

`Entities.kt` — `RecipeMetaEntity` gagne `val saisonMonths: List<Int>,` après `ingredientNames` ; les mappers `toEntity()`/`toDomain()` gagnent `saisonMonths = saisonMonths,`. `Converters` gagne :

```kotlin
    @TypeConverter
    fun intListToJson(value: List<Int>): String =
        jsonFmt.encodeToString<List<Int>>(value)

    @TypeConverter
    fun jsonToIntList(value: String): List<Int> =
        if (value.isBlank()) emptyList() else jsonFmt.decodeFromString<List<Int>>(value)
```

`CuisineDatabase.kt` — `version = 2,` et le builder gagne `.fallbackToDestructiveMigration()` avant `.build()` (cache uniquement, se re-fetch depuis Pages — pas de migration à écrire).

`HomeScreen.kt` — près de la déclaration des états (~ligne 137) :

```kotlin
    val month = remember { java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1 }
```

et dans le calcul `filtered` : `val byChip = filterByChip(recipes, chip, favorites, month).toSet()`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd android && ./gradlew testDebugUnitTest`
Expected: PASS (suite complète, y compris les 3 nouveaux).

- [ ] **Step 5: Commit**

```bash
git add android/app/src/main/java/fr/vferries/cuisine/data/RecipeMeta.kt \
        android/app/src/main/java/fr/vferries/cuisine/data/Chips.kt \
        android/app/src/main/java/fr/vferries/cuisine/data/cache/Entities.kt \
        android/app/src/main/java/fr/vferries/cuisine/data/cache/CuisineDatabase.kt \
        android/app/src/main/java/fr/vferries/cuisine/ui/HomeScreen.kt \
        android/app/src/test/java/fr/vferries/cuisine/data/ChipsTest.kt
git commit -m "feat(android): chip De saison, saisonMonths en cache Room

Room v2 en migration destructive (cache re-fetché depuis Pages) ;
mois courant injecté dans filterByChip."
```

---

### Task 4: Backfill des 15 recettes + saison requise — ⚠️ EXÉCUTION PAR LE CONTROLLER, VALIDATION OWNER OBLIGATOIRE

**Files:**
- Modify: `recipes/*.cook` (les 15 — metadata `>> saison:` ajoutée à l'en-tête)
- Modify: `tooling/src/validate-cook.ts` (REQUIRED_METADATA, suppression du warning provisoire)
- Modify: `tooling/src/validate-cook.test.ts`

**Interfaces:**
- Consumes: format et validation de la Task 1.
- Produces: les 15 recettes valides avec `saison:` ; metadata désormais requise (erreur).

⚠️ Cette task ne se délègue pas à un subagent : `recipes/` est le contenu de l'owner. Le controller lit les ingrédients de chaque recette, propose un tableau slug → saison suggérée, et attend la validation explicite de l'owner sur chaque valeur (AskUserQuestion ou tableau à corriger). AUCUN commit sans cette validation.

- [ ] **Step 1: Proposer le tableau** — lire les 15 `.cook`, proposer une saison par recette d'après la saisonnalité des ingrédients (girolles → automne, gaspacho → été, flan → toute l'année…), la soumettre à l'owner.
- [ ] **Step 2: Owner valide/corrige chaque valeur.** Attendre la réponse.
- [ ] **Step 3: Appliquer** — ajouter `>> saison: <valeur validée>` dans l'en-tête metadata de chaque `.cook` (après `>> course:` pour rester homogène).
- [ ] **Step 4: Rendre la metadata requise** — dans `validate-cook.ts` : ajouter `"saison",` à `REQUIRED_METADATA` (après `"course",`) et supprimer le bloc `if (!meta.saison) { warnings.push(...) }` (garder le `else if` de format, qui devient un `if (meta.saison && !isValidSaison(meta.saison))`). Dans `validate-cook.test.ts` : le test « warning si saison absente » devient :

```ts
  it("erreur si saison absente", () => {
    const { errors } = validateRecipe(base.join("\n"));
    expect(errors.some((e) => e.includes('"saison" manquante'))).toBe(true);
  });
```

et ajouter `">> saison: toute l'année",` au tableau `base` de CE describe est interdit (il teste l'absence) — en revanche, ajouter la ligne aux fixtures des AUTRES tests du fichier qui attendent zéro erreur.

- [ ] **Step 5: Vérifier** — `pnpm validate` (0 erreur, 0 warning saison) puis `pnpm test && pnpm test:e2e`.
- [ ] **Step 6: Commit**

```bash
git add recipes/*.cook tooling/src/validate-cook.ts tooling/src/validate-cook.test.ts
git commit -m "feat(recipes): saison sur les 15 recettes, metadata requise

Valeurs validées par l'owner ; le warning provisoire devient une erreur."
```

---

### Task 5: Docs — CONVENTIONS, import-recette, AGENTS

**Files:**
- Modify: `CONVENTIONS.md` (tableau « Requises », ~ligne 28)
- Modify: `.claude/skills/import-recette/SKILL.md` (liste de la salve, ~ligne 16)
- Modify: `AGENTS.md`

**Interfaces:** rien — cohérence documentaire.

- [ ] **Step 1: CONVENTIONS.md** — dans le tableau des métadonnées requises, après la ligne `course`, ajouter :

```markdown
| `saison` | plage | `toute l'année` · `octobre-mars` (mois français en minuscules, peut enjamber l'année, `mai-mai` = un seul mois) |
```

- [ ] **Step 2: SKILL.md import-recette** — dans la liste « À couvrir si manquant ou douteux » de la salve de questions, ajouter :

```markdown
- La `saison:` (`toute l'année` ou plage `<mois>-<mois>`) quand elle n'est pas évidente d'après les ingrédients — proposer une valeur dans la salve, jamais la décider seul.
```

- [ ] **Step 3: AGENTS.md** — dans « Idées en réserve », supprimer la ligne `- Chip « De saison » — metadata \`saison:\` optionnelle + comparaison au mois courant.` ; dans « Faits », ajouter :

```markdown
- [x] Chip « De saison » (web + Android) : metadata `saison:` requise (`toute l'année` ou plage `octobre-mars`), expandée en `saisonMonths` par build-index, prédicat à mois injecté (`FilterContext.month` web, param `month` Kotlin), Room v2 destructif. Spec : `docs/superpowers/specs/2026-07-15-chip-de-saison-design.md`.
```

- [ ] **Step 4: Commit**

```bash
git add CONVENTIONS.md .claude/skills/import-recette/SKILL.md AGENTS.md
git commit -m "docs: chip De saison livrée (conventions, import, AGENTS)"
```

---

## Self-Review

- Spec coverage : format+validation (T1), expansion build (T1), chip web AND filtres (T2 — le AND existant passe par l'intersection de Sets, inchangé), Android+Room destructif (T3), backfill owner-gated puis metadata requise (T4), CONVENTIONS/import/AGENTS (T5), e2e non déterministe évité (T2 : assertions de cohérence uniquement). OK.
- Placeholders : aucun — chaque étape code porte le code.
- Type consistency : `saisonMonths?: number[]` web (optionnel pour ne pas casser les fixtures existantes) vs `List<Int> = emptyList()` Kotlin (défaut pour les caches antérieurs) — sémantique identique « absent = liste vide » ; `month?: number` web ↔ `month: Int = 0` Kotlin, sentinelle « pas de filtre » des deux côtés.
