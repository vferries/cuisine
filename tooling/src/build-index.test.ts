import { describe, expect, it } from "vitest";
import { parseMinutes } from "./build-index.ts";

describe("parseMinutes", () => {
  it("parse les minutes simples", () => {
    expect(parseMinutes("20 min")).toBe(20);
  });

  it("parse les heures pleines", () => {
    expect(parseMinutes("3 h")).toBe(180);
  });

  it("parse heures + minutes", () => {
    expect(parseMinutes("1 h 10")).toBe(70);
  });

  it("retourne 0 sans valeur", () => {
    expect(parseMinutes(undefined)).toBe(0);
    expect(parseMinutes("")).toBe(0);
  });
});
