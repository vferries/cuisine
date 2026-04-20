import { describe, expect, it } from "vitest";
import { formatQty, formatUnit, pluralizeName } from "./format";

describe("formatUnit", () => {
  it("retourne '' pour undefined", () => {
    expect(formatUnit(1, undefined)).toBe("");
  });

  it("applique la map d'affichage càc → c. à c.", () => {
    expect(formatUnit(1, "càc")).toBe("c. à c.");
    expect(formatUnit(6, "càs")).toBe("c. à s.");
  });

  it("pluralise brin → brins quand qty > 1", () => {
    expect(formatUnit(2, "brin")).toBe("brins");
  });

  it("garde le singulier quand qty == 1", () => {
    expect(formatUnit(1, "brin")).toBe("brin");
  });

  it("garde le singulier quand qty < 1 (fraction)", () => {
    expect(formatUnit(0.5, "brin")).toBe("brin");
  });

  it("pluralise sachet, bouquet, gousse, pincée", () => {
    expect(formatUnit(3, "sachet")).toBe("sachets");
    expect(formatUnit(2, "bouquet")).toBe("bouquets");
    expect(formatUnit(4, "gousse")).toBe("gousses");
    expect(formatUnit(2, "pincée")).toBe("pincées");
  });

  it("ne pluralise pas les unités métriques invariantes", () => {
    expect(formatUnit(200, "g")).toBe("g");
    expect(formatUnit(2, "kg")).toBe("kg");
    expect(formatUnit(500, "ml")).toBe("ml");
    expect(formatUnit(6, "càc")).toBe("c. à c.");
  });

  it("retourne l'unité inconnue telle quelle sans pluraliser", () => {
    expect(formatUnit(5, "unknown")).toBe("unknown");
  });
});

describe("pluralizeName", () => {
  it("retourne le nom inchangé pour qty <= 1", () => {
    expect(pluralizeName(1, "poêle")).toBe("poêle");
    expect(pluralizeName(0, "poêle")).toBe("poêle");
  });

  it("ajoute 's' au nom simple quand qty > 1", () => {
    expect(pluralizeName(2, "poêle")).toBe("poêles");
    expect(pluralizeName(3, "bol")).toBe("bols");
  });

  it("laisse invariant un nom terminant déjà par s, x ou z", () => {
    expect(pluralizeName(2, "baguettes")).toBe("baguettes");
    expect(pluralizeName(2, "bois")).toBe("bois");
  });

  it("ne pluralise que le premier mot des noms composés", () => {
    expect(pluralizeName(2, "planche à découper")).toBe("planches à découper");
    expect(pluralizeName(2, "cuillère en bois")).toBe("cuillères en bois");
  });
});

describe("formatQty", () => {
  it("retourne null si qty est undefined ou vide", () => {
    expect(formatQty(undefined, "g")).toBeNull();
    expect(formatQty("", "g")).toBeNull();
  });

  it("retourne la qty seule quand aucune unité", () => {
    expect(formatQty(3, undefined)).toBe("3");
  });

  it("combine qty et unité formatée", () => {
    expect(formatQty(2, "brin")).toBe("2 brins");
    expect(formatQty(1, "brin")).toBe("1 brin");
    expect(formatQty(6, "càc")).toBe("6 c. à c.");
  });
});
