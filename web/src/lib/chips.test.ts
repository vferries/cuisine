import { describe, expect, it } from "vitest";
import {
  filterByChip,
  filterByCourse,
  filterByDifficulty,
  filterBySansGluten,
} from "./chips";

const porc = {
  slug: "porc-bigorre-caramel",
  cuisine: "vietnamienne",
  course: "plat",
  difficulty: "moyenne",
  tags: ["porc", "asiatique"],
  totalTime: 45,
};

const salade = {
  slug: "salade-tomate-mozza",
  cuisine: "italienne",
  course: "entrée",
  difficulty: "facile",
  tags: ["végé", "rapide"],
  totalTime: 10,
};

const tarte = {
  slug: "tarte-tatin",
  cuisine: "française",
  course: "dessert",
  difficulty: "difficile",
  tags: ["dessert"],
  totalTime: 60,
};

describe("filterByChip", () => {
  it("chip 'all' retourne tous les slugs", () => {
    expect(filterByChip([porc, salade], "all")).toEqual([
      "porc-bigorre-caramel",
      "salade-tomate-mozza",
    ]);
  });

  it("chip 'vege' ne retient que les recettes avec le tag 'végé'", () => {
    expect(filterByChip([porc, salade], "vege")).toEqual([
      "salade-tomate-mozza",
    ]);
  });

  it("chip 'rapide' ne retient que les recettes dont le total ≤ 30 min", () => {
    expect(filterByChip([porc, salade, tarte], "rapide")).toEqual([
      "salade-tomate-mozza",
    ]);
  });

  it("chip 'asiatique' ne retient que les recettes avec le tag 'asiatique'", () => {
    expect(filterByChip([porc, salade, tarte], "asiatique")).toEqual([
      "porc-bigorre-caramel",
    ]);
  });

  it("chip 'francais' ne retient que les recettes de cuisine française", () => {
    expect(filterByChip([porc, salade, tarte], "francais")).toEqual([
      "tarte-tatin",
    ]);
  });

  it("chip 'dessert' ne retient que les recettes avec le tag 'dessert'", () => {
    expect(filterByChip([porc, salade, tarte], "dessert")).toEqual([
      "tarte-tatin",
    ]);
  });

  it("chip 'favoris' ne retient que les recettes dont le slug est marqué favori", () => {
    expect(
      filterByChip([porc, salade, tarte], "favoris", { favorites: ["tarte-tatin", "porc-bigorre-caramel"] }),
    ).toEqual(["porc-bigorre-caramel", "tarte-tatin"]);
  });

  it("chip 'favoris' retourne [] si aucun favori connu", () => {
    expect(filterByChip([porc, salade, tarte], "favoris", { favorites: [] })).toEqual([]);
  });
});

describe("filterByCourse", () => {
  it("retourne tous les slugs quand aucune course n'est sélectionnée", () => {
    expect(filterByCourse([porc, salade, tarte], null)).toEqual([
      "porc-bigorre-caramel",
      "salade-tomate-mozza",
      "tarte-tatin",
    ]);
  });

  it("ne retient que les recettes dont course = 'entrée'", () => {
    expect(filterByCourse([porc, salade, tarte], "entrée")).toEqual([
      "salade-tomate-mozza",
    ]);
  });

  it("ne retient que les recettes dont course = 'plat'", () => {
    expect(filterByCourse([porc, salade, tarte], "plat")).toEqual([
      "porc-bigorre-caramel",
    ]);
  });

  it("ne retient que les recettes dont course = 'dessert'", () => {
    expect(filterByCourse([porc, salade, tarte], "dessert")).toEqual([
      "tarte-tatin",
    ]);
  });
});

describe("filterByDifficulty", () => {
  it("retourne tous les slugs quand aucune difficulté n'est sélectionnée", () => {
    expect(filterByDifficulty([porc, salade, tarte], null)).toEqual([
      "porc-bigorre-caramel",
      "salade-tomate-mozza",
      "tarte-tatin",
    ]);
  });

  it("ne retient que les recettes dont difficulty = 'facile'", () => {
    expect(filterByDifficulty([porc, salade, tarte], "facile")).toEqual([
      "salade-tomate-mozza",
    ]);
  });
});

describe("filterBySansGluten", () => {
  const flan = {
    slug: "flan",
    cuisine: "française",
    course: "dessert",
    difficulty: "facile",
    tags: ["dessert", "sans gluten"],
    totalTime: 80,
  };

  it("inactif: retourne tous les slugs", () => {
    expect(filterBySansGluten([porc, flan], false)).toEqual([
      "porc-bigorre-caramel",
      "flan",
    ]);
  });

  it("actif: ne retient que les recettes taguées 'sans gluten'", () => {
    expect(filterBySansGluten([porc, flan], true)).toEqual(["flan"]);
  });
});

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
