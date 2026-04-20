import { describe, expect, it, vi } from "vitest";
import {
  acquireWakeLock,
  flattenSteps,
  formatSecondsAsTime,
  timerSeconds,
} from "./cuisson";

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

  it("expose sectionIdx et stepIdx pour composer des identifiants stables", () => {
    const flat = flattenSteps([
      { name: "Préparation", steps: [step("a"), step("b")] },
      { name: "Cuisson", steps: [step("c")] },
    ]);
    expect(flat.map((f) => [f.sectionIdx, f.stepIdx])).toEqual([
      [0, 0],
      [0, 1],
      [1, 0],
    ]);
  });
});

describe("timerSeconds", () => {
  it("convertit les minutes en secondes", () => {
    expect(timerSeconds({ quantity: 3, unit: "min" })).toBe(180);
  });

  it("retourne les secondes telles quelles", () => {
    expect(timerSeconds({ quantity: 45, unit: "sec" })).toBe(45);
  });

  it("convertit les heures en secondes", () => {
    expect(timerSeconds({ quantity: 1, unit: "h" })).toBe(3600);
  });
});

describe("formatSecondsAsTime", () => {
  it("formate 3 minutes rondes en M:SS", () => {
    expect(formatSecondsAsTime(180)).toBe("3:00");
  });

  it("formate 2 min 5 sec en M:SS", () => {
    expect(formatSecondsAsTime(125)).toBe("2:05");
  });

  it("formate 59 sec en 0:59", () => {
    expect(formatSecondsAsTime(59)).toBe("0:59");
  });
});

describe("acquireWakeLock", () => {
  it("retourne null quand l'API wakeLock n'est pas disponible", async () => {
    expect(await acquireWakeLock({})).toBeNull();
  });

  it("appelle navigator.wakeLock.request('screen') et renvoie le sentinel", async () => {
    const sentinel = { released: false };
    const request = vi.fn(async () => sentinel);
    const nav = { wakeLock: { request } };

    const result = await acquireWakeLock(nav as any);

    expect(request).toHaveBeenCalledWith("screen");
    expect(result).toBe(sentinel);
  });

  it("retourne null si la requête est rejetée", async () => {
    const request = vi.fn(async () => {
      throw new Error("NotAllowed");
    });
    const nav = { wakeLock: { request } };

    expect(await acquireWakeLock(nav as any)).toBeNull();
  });
});
