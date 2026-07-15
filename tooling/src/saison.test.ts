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
