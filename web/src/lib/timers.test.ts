import { describe, expect, it } from "vitest";
import {
  addTimer,
  isExpired,
  remainingSeconds,
  removeTimer,
  type Timer,
} from "./timers";

const make = (overrides: Partial<Timer> = {}): Timer => ({
  id: "t1",
  name: "Cuisson",
  durationSeconds: 180,
  startedAt: 0,
  ...overrides,
});

describe("remainingSeconds", () => {
  it("renvoie la durée pleine à t=0", () => {
    expect(remainingSeconds(make(), 0)).toBe(180);
  });

  it("décrémente à la seconde près", () => {
    expect(remainingSeconds(make(), 1000)).toBe(179);
    expect(remainingSeconds(make(), 60_000)).toBe(120);
  });

  it("devient négatif après expiration (pas clampé ici)", () => {
    expect(remainingSeconds(make(), 200_000)).toBe(-20);
  });
});

describe("isExpired", () => {
  it("faux tant que remaining > 0", () => {
    expect(isExpired(make(), 0)).toBe(false);
    expect(isExpired(make(), 179_000)).toBe(false);
  });

  it("vrai dès que remaining <= 0", () => {
    expect(isExpired(make(), 180_000)).toBe(true);
    expect(isExpired(make(), 300_000)).toBe(true);
  });
});

describe("addTimer", () => {
  it("ajoute un timer à la liste", () => {
    expect(addTimer([], make())).toEqual([make()]);
  });

  it("ignore un timer dont l'id existe déjà (idempotent)", () => {
    const existing = make({ id: "t1", startedAt: 0 });
    const next = make({ id: "t1", startedAt: 5000 });
    expect(addTimer([existing], next)).toEqual([existing]);
  });

  it("préserve l'ordre des timers existants", () => {
    const a = make({ id: "a" });
    const b = make({ id: "b" });
    const c = make({ id: "c" });
    expect(addTimer([a, b], c)).toEqual([a, b, c]);
  });
});

describe("removeTimer", () => {
  it("retire le timer par id", () => {
    const a = make({ id: "a" });
    const b = make({ id: "b" });
    expect(removeTimer([a, b], "a")).toEqual([b]);
  });

  it("no-op si l'id n'existe pas", () => {
    const a = make({ id: "a" });
    expect(removeTimer([a], "z")).toEqual([a]);
  });
});
