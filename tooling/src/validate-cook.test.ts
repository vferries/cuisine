import { describe, expect, it } from "vitest";
import { validateRecipe } from "./validate-cook.ts";

const validRecipe = `>> title: Recette test
>> servings: 2
>> prep time: 10 min
>> cook time: 20 min
>> difficulty: facile
>> cuisine: française

Une étape.`;

describe("validateRecipe", () => {
  it("renvoie 0 erreur pour une recette avec les 6 metadata requises", () => {
    const result = validateRecipe(validRecipe);
    expect(result.errors).toEqual([]);
  });

  it("signale chaque metadata requise manquante", () => {
    const result = validateRecipe("");
    for (const key of ["title", "servings", "prep time", "cook time", "difficulty", "cuisine"]) {
      expect(result.errors.some((e) => e.includes(key))).toBe(true);
    }
  });

  it("rejette une difficulty hors de l'énum {facile, moyenne, difficile}", () => {
    const source = validRecipe.replace("difficulty: facile", "difficulty: impossible");
    const result = validateRecipe(source);
    expect(result.errors.some((e) => e.includes("difficulty"))).toBe(true);
  });

  it("accepte les trois valeurs valides de difficulty", () => {
    for (const d of ["facile", "moyenne", "difficile"]) {
      const source = validRecipe.replace("difficulty: facile", `difficulty: ${d}`);
      expect(validateRecipe(source).errors).toEqual([]);
    }
  });

  it("rejette un servings non-numérique", () => {
    const source = validRecipe.replace("servings: 2", "servings: deux");
    const result = validateRecipe(source);
    expect(result.errors.some((e) => e.includes("servings"))).toBe(true);
  });

  it("rejette un servings négatif ou nul", () => {
    const source = validRecipe.replace("servings: 2", "servings: 0");
    const result = validateRecipe(source);
    expect(result.errors.some((e) => e.includes("servings"))).toBe(true);
  });

  it("warn si tags absent", () => {
    const result = validateRecipe(validRecipe);
    expect(result.warnings.some((w) => w.includes("tags"))).toBe(true);
  });

  it("warn si tags vide (virgules uniquement)", () => {
    const source = validRecipe.replace(
      ">> cuisine: française",
      ">> cuisine: française\n>> tags: ,, ,",
    );
    const result = validateRecipe(source);
    expect(result.warnings.some((w) => w.includes("tags"))).toBe(true);
  });

  it("pas de warning tags quand au moins un tag est présent", () => {
    const source = validRecipe.replace(
      ">> cuisine: française",
      ">> cuisine: française\n>> tags: porc, asiatique",
    );
    const result = validateRecipe(source);
    expect(result.warnings.some((w) => w.includes("tags"))).toBe(false);
  });

  it("warn si source absent", () => {
    const result = validateRecipe(validRecipe);
    expect(result.warnings.some((w) => w.includes("source"))).toBe(true);
  });

  it("pas de warning source quand source présente", () => {
    const source = validRecipe.replace(
      ">> cuisine: française",
      ">> cuisine: française\n>> source: Maman",
    );
    const result = validateRecipe(source);
    expect(result.warnings.some((w) => w.includes("source"))).toBe(false);
  });

  const withStep = (stepText: string) =>
    validRecipe.replace("Une étape.", stepText);

  it("pas de warning pour une unité d'ingrédient autorisée", () => {
    const source = withStep("Ajoutez @sucre{3%càc}.");
    const result = validateRecipe(source);
    expect(result.warnings.some((w) => w.includes("unité"))).toBe(false);
  });

  it("warn pour une unité d'ingrédient hors liste autorisée", () => {
    const source = withStep("Ajoutez @sucre{3%xyz}.");
    const result = validateRecipe(source);
    expect(
      result.warnings.some((w) => w.includes("unité") && w.includes("xyz")),
    ).toBe(true);
  });

  it("pas de warning pour une unité de timer autorisée", () => {
    const source = withStep("Faites cuire ~cuisson{10%min}.");
    const result = validateRecipe(source);
    expect(result.warnings.some((w) => w.includes("timer"))).toBe(false);
  });

  it("warn pour une unité de timer hors liste", () => {
    const source = withStep("Faites cuire ~cuisson{10%lightyear}.");
    const result = validateRecipe(source);
    expect(
      result.warnings.some(
        (w) => w.includes("timer") && w.includes("lightyear"),
      ),
    ).toBe(true);
  });

  it("accepte les pluriels des unités de comptage", () => {
    for (const unit of ["brins", "bouquets", "sachets", "gousses", "pincées"]) {
      const source = withStep(`Ajoutez @chose{2%${unit}}.`);
      const result = validateRecipe(source);
      expect(
        result.warnings.some((w) => w.includes("unité") && w.includes(unit)),
      ).toBe(false);
    }
  });

  it("erreur si la recette n'a aucune étape parsée", () => {
    const metaOnly = validRecipe.replace("\nUne étape.", "");
    const result = validateRecipe(metaOnly);
    expect(result.errors.some((e) => e.includes("étape"))).toBe(true);
  });

  it("erreur quand image référencée mais fichier absent", () => {
    const source = validRecipe.replace(
      ">> cuisine: française",
      ">> cuisine: française\n>> image: test.webp",
    );
    const result = validateRecipe(source, { imageExists: () => false });
    expect(result.errors.some((e) => e.includes("image"))).toBe(true);
  });

  it("pas d'erreur quand image référencée et fichier présent", () => {
    const source = validRecipe.replace(
      ">> cuisine: française",
      ">> cuisine: française\n>> image: test.webp",
    );
    const imageExists = (name: string) => name === "test.webp";
    const result = validateRecipe(source, { imageExists });
    expect(result.errors.some((e) => e.includes("image"))).toBe(false);
  });

  it("skip la vérif image quand imageExists n'est pas fourni", () => {
    const source = validRecipe.replace(
      ">> cuisine: française",
      ">> cuisine: française\n>> image: test.webp",
    );
    const result = validateRecipe(source);
    expect(result.errors.some((e) => e.includes("image"))).toBe(false);
  });
});
