import { describe, expect, it } from "vitest";
import { pickRandom } from "./random";

describe("pickRandom", () => {
  it("retourne undefined sur une liste vide", () => {
    expect(pickRandom([], () => 0)).toBeUndefined();
  });

  it("rng=0 → premier élément", () => {
    expect(pickRandom(["a", "b", "c"], () => 0)).toBe("a");
  });

  it("rng→0.999… → dernier élément", () => {
    expect(pickRandom(["a", "b", "c"], () => 0.999)).toBe("c");
  });

  it("rng=0.5 sur 4 éléments → index 2", () => {
    expect(pickRandom(["a", "b", "c", "d"], () => 0.5)).toBe("c");
  });

  it("utilise Math.random par défaut et reste dans la liste", () => {
    const items = ["a", "b", "c"];
    for (let i = 0; i < 50; i++) {
      expect(items).toContain(pickRandom(items));
    }
  });
});
