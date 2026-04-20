import { describe, expect, it } from "vitest";
import { withBase } from "./url";

describe("withBase", () => {
  it("concatène base root '/' et slug sans doubler le séparateur", () => {
    expect(withBase("/", "porc")).toBe("/porc");
  });

  it("concatène base subpath '/cuisine' (sans trailing) et slug", () => {
    expect(withBase("/cuisine", "porc")).toBe("/cuisine/porc");
  });

  it("concatène base subpath '/cuisine/' (avec trailing) et slug", () => {
    expect(withBase("/cuisine/", "porc")).toBe("/cuisine/porc");
  });

  it("tolère un slug commençant par '/' sans doubler", () => {
    expect(withBase("/cuisine", "/porc")).toBe("/cuisine/porc");
  });

  it("retourne la racine propre quand le path est vide ou '/'", () => {
    expect(withBase("/", "")).toBe("/");
    expect(withBase("/cuisine", "")).toBe("/cuisine/");
    expect(withBase("/cuisine", "/")).toBe("/cuisine/");
  });

  it("concatène un sous-chemin multi-segments", () => {
    expect(withBase("/cuisine", "cuisson/porc")).toBe("/cuisine/cuisson/porc");
  });
});
