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
});
