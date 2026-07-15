import { describe, expect, it } from "vitest";
import { sortRecipes } from "./sort";

const a = {
  slug: "a-bouillon",
  title: "Bouillon de châtaigne",
  totalTime: 40,
  updatedAt: "2026-04-10T00:00:00.000Z",
};

const b = {
  slug: "b-flan",
  title: "Flan caramel",
  totalTime: 80,
  updatedAt: "2026-04-20T00:00:00.000Z",
};

const c = {
  slug: "c-katsu",
  title: "Aubergine miso",
  totalTime: 25,
  updatedAt: "2026-04-15T00:00:00.000Z",
};

describe("sortRecipes", () => {
  it("'recent' trie par updatedAt décroissant", () => {
    expect(sortRecipes([a, b, c], "recent")).toEqual([
      "b-flan",
      "c-katsu",
      "a-bouillon",
    ]);
  });

  it("'alpha' trie par titre croissant (collation FR)", () => {
    expect(sortRecipes([a, b, c], "alpha")).toEqual([
      "c-katsu",
      "a-bouillon",
      "b-flan",
    ]);
  });

  it("'duration' trie par totalTime croissant", () => {
    expect(sortRecipes([a, b, c], "duration")).toEqual([
      "c-katsu",
      "a-bouillon",
      "b-flan",
    ]);
  });

  it("'recent' départage les updatedAt identiques par slug", () => {
    const t = "2026-04-28T07:05:10.000Z";
    const x = { ...a, slug: "z-dernier", updatedAt: t };
    const y = { ...b, slug: "a-premier", updatedAt: t };
    const z = { ...c, slug: "m-milieu", updatedAt: t };
    expect(sortRecipes([x, y, z], "recent")).toEqual([
      "a-premier",
      "m-milieu",
      "z-dernier",
    ]);
    expect(sortRecipes([z, x, y], "recent")).toEqual([
      "a-premier",
      "m-milieu",
      "z-dernier",
    ]);
  });

  it("'duration' départage les totalTime identiques par slug", () => {
    const x = { ...a, slug: "z-dernier", totalTime: 30 };
    const y = { ...b, slug: "a-premier", totalTime: 30 };
    expect(sortRecipes([x, y], "duration")).toEqual([
      "a-premier",
      "z-dernier",
    ]);
    expect(sortRecipes([y, x], "duration")).toEqual([
      "a-premier",
      "z-dernier",
    ]);
  });

  it("ne mute pas l'argument d'entrée", () => {
    const arr = [a, b, c];
    sortRecipes(arr, "alpha");
    expect(arr.map((r) => r.slug)).toEqual([
      "a-bouillon",
      "b-flan",
      "c-katsu",
    ]);
  });
});
