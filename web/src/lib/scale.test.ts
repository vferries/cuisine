import { describe, expect, it } from "vitest";
import { scaleQuantity } from "./scale";

describe("scaleQuantity", () => {
  it("multiplie une quantité entière par le ratio", () => {
    expect(scaleQuantity(6, 1.5)).toBe(9);
  });
});
