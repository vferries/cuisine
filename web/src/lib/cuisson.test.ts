import { describe, expect, it } from "vitest";
import { flattenSteps } from "./cuisson";

const step = (text: string) => ({ tokens: [{ type: "text" as const, text }] });

describe("flattenSteps", () => {
  it("retourne toutes les étapes dans l'ordre, à travers les sections", () => {
    const flat = flattenSteps([
      { name: "Préparation", steps: [step("a"), step("b")] },
      { name: "Cuisson", steps: [step("c")] },
    ]);
    expect(flat).toHaveLength(3);
    expect(flat.map((f) => f.section)).toEqual([
      "Préparation",
      "Préparation",
      "Cuisson",
    ]);
  });
});
