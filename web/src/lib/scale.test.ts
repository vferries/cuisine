import { describe, expect, it } from "vitest";
import { scaleQuantity } from "./scale";

describe("scaleQuantity", () => {
  it("multiplie une quantité entière par le ratio", () => {
    expect(scaleQuantity(6, 1.5)).toBe(9);
  });

  it("arrondit à 2 décimales comme Android (scaleQuantityText)", () => {
    expect(scaleQuantity(700, 7 / 6)).toBe(816.67);
    expect(scaleQuantity(1000, 7 / 6)).toBe(1166.67);
  });

  it("évite les artefacts flottants", () => {
    expect(scaleQuantity(1125, 2 / 3)).toBe(750);
    expect(scaleQuantity(0.1, 3)).toBe(0.3);
  });
});
