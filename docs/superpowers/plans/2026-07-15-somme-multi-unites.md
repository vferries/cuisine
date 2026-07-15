# Somme multi-unités (g/kg, ml/l) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sommer les quantités d'ingrédients dupliqués à travers g/kg et ml/l (500 g + 1 kg → « 1.5 kg »), au build et au scaling live des portions.

**Architecture:** Le parser (`tooling`) normalise en unité de base (kg→g ×1000, l→ml ×1000) pendant l'agrégation, la somme existante à unité identique fait le reste. L'humanisation (≥ 1000 g → kg, ≥ 1000 ml → l) vit dans les formatters d'affichage : `formatQty` web (couvre rendu initial + re-scaling live + mode cuisson) et son miroir Kotlin `Format.kt`. Spec : `docs/superpowers/specs/2026-07-15-somme-multi-unites-design.md`.

**Tech Stack:** TypeScript strict, Vitest (tooling + web), Kotlin + JUnit (android).

## Global Constraints

- Pas de Playwright dédié : exigerait une vraie recette avec duplication multi-unités, or `recipes/` = contenu owner, jamais inventé. Fixtures inline dans les tests uniquement.
- Humanisation vers le haut uniquement (jamais 500 ml → 0,5 l).
- Arrondi à 3 décimales max après division par 1000.
- Les tokens des étapes gardent l'unité écrite par l'auteur (seule la liste agrégée est normalisée).
- Commits courts (sujet + 2-4 lignes de corps max), pas de Co-Authored-By, pas de push.
- Le validator ne change pas (g/kg/ml/l déjà autorisés).

---

### Task 1: Parser — normalisation et somme multi-unités

**Files:**
- Modify: `tooling/src/parser.ts` (boucle d'agrégation, ~ligne 277)
- Test: `tooling/src/parser.test.ts`

**Interfaces:**
- Consumes: `parseCook(source: string): ParsedRecipe` existant.
- Produces: `ParsedRecipe.ingredients` où toute quantité numérique en kg ou l est convertie en g ou ml (`{ name: "porc", quantity: 1500, unit: "g" }`). Signature inchangée. Les tasks 2-3 s'appuient sur le fait que la liste agrégée est en unité de base.

- [ ] **Step 1: Write the failing tests**

Ajouter à la fin de `tooling/src/parser.test.ts` :

```ts
describe("parseCook — somme multi-unités", () => {
  it("somme 500 g + 1 kg en 1500 g", () => {
    const src = [
      "== Test ==",
      "",
      "Saisir @porc{500%g}.",
      "",
      "Ajouter @porc{1%kg}.",
      "",
    ].join("\n");
    const r = parseCook(src);
    expect(r.ingredients).toEqual([
      { name: "porc", quantity: 1500, unit: "g" },
    ]);
  });

  it("somme 200 ml + 1 l en 1200 ml", () => {
    const src = "== Test ==\n\nVerser @lait{200%ml} puis @lait{1%l}.\n";
    const r = parseCook(src);
    expect(r.ingredients).toEqual([
      { name: "lait", quantity: 1200, unit: "ml" },
    ]);
  });

  it("normalise une occurrence unique en unité de base (1 kg → 1000 g)", () => {
    const r = parseCook("== Test ==\n\nPeser @farine{1%kg}.\n");
    expect(r.ingredients).toEqual([
      { name: "farine", quantity: 1000, unit: "g" },
    ]);
  });

  it("garde la 1ère occurrence quand les unités ne sont pas convertibles entre elles", () => {
    const src = "== Test ==\n\n@levure{1%kg} et @levure{1%sachet}.\n";
    const r = parseCook(src);
    expect(r.ingredients).toEqual([
      { name: "levure", quantity: 1000, unit: "g" },
    ]);
  });

  it("laisse les quantités texte inchangées", () => {
    const r = parseCook("== Test ==\n\nSaler @sel{au goût%g}.\n");
    expect(r.ingredients).toEqual([
      { name: "sel", quantity: "au goût", unit: "g" },
    ]);
  });

  it("ne touche pas aux tokens des étapes (l'auteur garde son unité)", () => {
    const r = parseCook("== Test ==\n\nPeser @farine{1%kg}.\n");
    const tok = r.sections[0].steps[0].tokens.find(
      (t) => t.type === "ingredient",
    );
    expect(tok).toEqual({
      type: "ingredient",
      ingredient: { name: "farine", quantity: 1, unit: "kg" },
    });
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `pnpm --filter @cuisine/tooling test`
Expected: FAIL — les 4 premiers nouveaux tests échouent sur les quantités/unités (ex. reçu `{ quantity: 500, unit: "g" }` au lieu de `1500`), pas sur un import manquant. Les 2 derniers (texte, tokens) peuvent déjà passer.

- [ ] **Step 3: Write minimal implementation**

Dans `tooling/src/parser.ts`, ajouter au-dessus de `export function parseCook` :

```ts
const BASE_UNITS: Record<string, { base: string; factor: number }> = {
  kg: { base: "g", factor: 1000 },
  l: { base: "ml", factor: 1000 },
};

function toBaseUnit(ing: Ingredient): Ingredient {
  if (typeof ing.quantity !== "number" || !ing.unit) return ing;
  const conv = BASE_UNITS[ing.unit];
  if (!conv) return ing;
  return { ...ing, quantity: ing.quantity * conv.factor, unit: conv.base };
}
```

Puis dans la boucle d'agrégation, remplacer :

```ts
        if (tok.type === "ingredient") {
          const ing = tok.ingredient;
```

par :

```ts
        if (tok.type === "ingredient") {
          const ing = toBaseUnit(tok.ingredient);
```

Le reste de la logique (somme si `prev.unit === ing.unit` et quantités numériques) est inchangé.

- [ ] **Step 4: Run tests to verify they pass**

Run: `pnpm --filter @cuisine/tooling test`
Expected: PASS (tous, y compris les tests existants d'agrégation à unité identique).

- [ ] **Step 5: Commit**

```bash
git add tooling/src/parser.ts tooling/src/parser.test.ts
git commit -m "feat(parser): somme multi-unités g/kg et ml/l

L'agrégation normalise kg→g et l→ml avant insertion ;
la somme à unité identique existante fait le reste."
```

---

### Task 2: Web — humanisation kg/l dans formatQty

**Files:**
- Modify: `web/src/lib/format.ts`
- Test: `web/src/lib/format.test.ts`

**Interfaces:**
- Consumes: liste agrégée en unité de base produite par la Task 1 (via `web/src/generated/`).
- Produces: `formatQty(qty: number | string | undefined, unit: string | undefined): string | null` — signature inchangée, mais `formatQty(1500, "g")` rend désormais `"1.5 kg"`. Rendu initial, re-scaling live et mode cuisson passent déjà tous par `formatQty` : aucun autre fichier web à modifier.

- [ ] **Step 1: Write the failing tests**

Ajouter à la fin de `web/src/lib/format.test.ts` :

```ts
describe("formatQty — humanisation g→kg et ml→l", () => {
  it("bascule en kg dès 1000 g", () => {
    expect(formatQty(1500, "g")).toBe("1.5 kg");
    expect(formatQty(1000, "g")).toBe("1 kg");
  });

  it("reste en g sous 1000", () => {
    expect(formatQty(999, "g")).toBe("999 g");
  });

  it("bascule en l dès 1000 ml", () => {
    expect(formatQty(1200, "ml")).toBe("1.2 l");
  });

  it("ne convertit jamais vers le bas", () => {
    expect(formatQty(0.5, "l")).toBe("0.5 l");
    expect(formatQty(500, "ml")).toBe("500 ml");
  });

  it("arrondit à 3 décimales max", () => {
    expect(formatQty(1125, "g")).toBe("1.125 kg");
  });

  it("laisse les quantités texte inchangées", () => {
    expect(formatQty("au goût", "g")).toBe("au goût g");
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `pnpm --filter @cuisine/web test`
Expected: FAIL — `"1500 g"` reçu au lieu de `"1.5 kg"` (échec d'assertion, pas d'import).

- [ ] **Step 3: Write minimal implementation**

Dans `web/src/lib/format.ts`, ajouter au-dessus de `formatQty` :

```ts
const HUMANIZE_UP: Record<string, { unit: string; factor: number }> = {
  g: { unit: "kg", factor: 1000 },
  ml: { unit: "l", factor: 1000 },
};

function humanize(
  qty: number | string | undefined,
  unit: string | undefined,
): [number | string | undefined, string | undefined] {
  const up = unit ? HUMANIZE_UP[unit] : undefined;
  const n = typeof qty === "number" ? qty : Number(qty);
  if (!up || !Number.isFinite(n) || n < up.factor) return [qty, unit];
  return [Math.round((n / up.factor) * 1000) / 1000, up.unit];
}
```

Et remplacer le corps de `formatQty` :

```ts
export function formatQty(
  qty: number | string | undefined,
  unit: string | undefined,
): string | null {
  if (qty === undefined || qty === "") return null;
  const [q, u] = humanize(qty, unit);
  const display = formatUnit(q, u);
  return display ? `${q} ${display}` : String(q);
}
```

`formatUnit` reste inchangé (kg et l ne sont pas pluralisables).

- [ ] **Step 4: Run tests to verify they pass**

Run: `pnpm --filter @cuisine/web test`
Expected: PASS (tous).

- [ ] **Step 5: Full check web (e2e inclus)**

Run: `pnpm validate && pnpm test && pnpm test:e2e`
Expected: PASS — vérifie qu'aucun spec Playwright existant ne dépend d'un affichage « 1500 g ».

- [ ] **Step 6: Commit**

```bash
git add web/src/lib/format.ts web/src/lib/format.test.ts
git commit -m "feat(web): formatQty humanise g→kg et ml→l dès 1000

Couvre la sidebar, les étapes, le mode cuisson et le
re-scaling live, tous branchés sur formatQty."
```

---

### Task 3: Android — humanisation kg/l dans Format.kt

**Files:**
- Modify: `android/app/src/main/java/fr/vferries/cuisine/data/Format.kt`
- Test: `android/app/src/test/java/fr/vferries/cuisine/data/FormatTest.kt`

**Interfaces:**
- Consumes: quantités String du JSON (déjà en unité de base après Task 1, redéployé côté Pages).
- Produces: `formatQty(qty: String, unit: String?): String?` — signature inchangée, `formatQty("1500", "g")` rend `"1.5 kg"`. Le scaling (`scaleQuantityText`) s'exécute avant `formatQty`, l'humanisation s'applique donc aussi au scaling live.

- [ ] **Step 1: Write the failing tests**

Ajouter dans `FormatTest.kt` (dans la classe) :

```kotlin
    @Test fun formatQty_humanizes_g_to_kg_from_1000() {
        assertEquals("1.5 kg", formatQty("1500", "g"))
        assertEquals("1 kg", formatQty("1000", "g"))
        assertEquals("999 g", formatQty("999", "g"))
    }

    @Test fun formatQty_humanizes_ml_to_l_from_1000() {
        assertEquals("1.2 l", formatQty("1200", "ml"))
    }

    @Test fun formatQty_never_converts_down() {
        assertEquals("0.5 l", formatQty("0.5", "l"))
        assertEquals("500 ml", formatQty("500", "ml"))
    }

    @Test fun formatQty_rounds_to_3_decimals() {
        assertEquals("1.125 kg", formatQty("1125", "g"))
    }

    @Test fun formatQty_leaves_text_quantities() {
        assertEquals("au goût g", formatQty("au goût", "g"))
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd android && ./gradlew testDebugUnitTest --tests "fr.vferries.cuisine.data.FormatTest"`
Expected: FAIL — `expected:<1.5 kg> but was:<1500 g>` (assertion, pas compilation).

- [ ] **Step 3: Write minimal implementation**

Dans `Format.kt`, ajouter au-dessus de `formatQty` :

```kotlin
private val HUMANIZE_UP = mapOf(
    "g" to ("kg" to 1000.0),
    "ml" to ("l" to 1000.0),
)

private fun formatNumber(n: Double): String {
    val rounded = Math.round(n * 1000).toDouble() / 1000.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString()
    else rounded.toString()
}

/** g→kg / ml→l dès 1000 ; qty/unit inchangés sinon. */
private fun humanize(qty: String, unit: String?): Pair<String, String?> {
    val up = unit?.let { HUMANIZE_UP[it] } ?: return qty to unit
    val n = qty.toDoubleOrNull() ?: return qty to unit
    if (n < up.second) return qty to unit
    return formatNumber(n / up.second) to up.first
}
```

Et remplacer le corps de `formatQty` :

```kotlin
/** Returns "<qty> <unit>" (ou "<qty>" ou null si qty vide), humanisé kg/l. */
fun formatQty(qty: String, unit: String?): String? {
    if (qty.isEmpty()) return null
    val (q, u) = humanize(qty, unit)
    val display = formatUnit(q, u)
    return if (display.isEmpty()) q else "$q $display"
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd android && ./gradlew testDebugUnitTest --tests "fr.vferries.cuisine.data.FormatTest"`
Expected: PASS. Puis `./gradlew testDebugUnitTest` complet : PASS.

- [ ] **Step 5: Commit**

```bash
git add android/app/src/main/java/fr/vferries/cuisine/data/Format.kt \
        android/app/src/test/java/fr/vferries/cuisine/data/FormatTest.kt
git commit -m "feat(android): formatQty humanise g→kg et ml→l dès 1000

Miroir de la règle web ; s'applique après scaleQuantityText
donc aussi au scaling live des portions."
```

---

### Task 4: Docs — AGENTS.md à jour

**Files:**
- Modify: `AGENTS.md`

**Interfaces:**
- Consumes: rien.
- Produces: mémoire projet cohérente avec le code.

- [ ] **Step 1: Mettre à jour AGENTS.md**

Dans « Limites connues du parser », remplacer :

```
- La somme des quantités dupliquées ne marche que si l'unité est identique.
```

par :

```
- La somme des quantités dupliquées couvre l'unité identique + les familles g/kg et ml/l (normalisées en unité de base au parse, humanisées kg/l à l'affichage dès 1000 — web `format.ts`, Android `Format.kt`).
```

Dans « Idées en réserve », supprimer la ligne :

```
- Somme multi-unités dans le parser (500 g + 1 kg) — conversions g/kg et ml/l.
```

Dans « Faits », ajouter :

```
- [x] Somme multi-unités (500 g + 1 kg → 1,5 kg) : parser normalise g/kg et ml/l en unité de base, `formatQty` web + Android humanisent dès 1000, y compris au scaling live. Spec : `docs/superpowers/specs/2026-07-15-somme-multi-unites-design.md`.
```

Dans « État actuel du code », remplacer la ligne « Une recette de référence : … » par :

```
- 15 recettes dans `recipes/` (importées via /import-recette).
```

- [ ] **Step 2: Commit**

```bash
git add AGENTS.md
git commit -m "docs: somme multi-unités livrée, AGENTS à jour"
```

---

## Self-Review

- Spec coverage : familles g/kg + ml/l (T1), humanisation ≥ 1000 web (T2) et Android (T3), arrondi 3 décimales (T2/T3), tokens d'étapes intacts (T1 test dédié), validator intact (aucune task ne le touche), tous les cas de test du spec répartis T1-T3. OK.
- Placeholders : aucun.
- Types : `formatQty` web garde `number | string | undefined` ; Kotlin garde `String` ; `humanize` cohérent avec `formatUnit(q, u)` dans les deux mondes. OK.
