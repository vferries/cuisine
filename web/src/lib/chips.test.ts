import { describe, expect, it } from "vitest";
import { filterByChip } from "./chips";

const porc = {
  slug: "porc-bigorre-caramel",
  cuisine: "vietnamienne",
  tags: ["porc", "asiatique"],
  totalTime: 45,
};

const salade = {
  slug: "salade-tomate-mozza",
  cuisine: "italienne",
  tags: ["végé", "rapide"],
  totalTime: 10,
};

const tarte = {
  slug: "tarte-tatin",
  cuisine: "française",
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
