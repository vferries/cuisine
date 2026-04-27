import { describe, expect, it, vi } from "vitest";
import { shareRecipe } from "./share";

describe("shareRecipe", () => {
  it("appelle deps.share avec title + url quand disponible", async () => {
    const share = vi.fn().mockResolvedValue(undefined);
    const copy = vi.fn();
    const result = await shareRecipe(
      { title: "Porc", url: "https://x/porc" },
      { share, copy },
    );
    expect(share).toHaveBeenCalledWith({
      title: "Porc",
      url: "https://x/porc",
    });
    expect(copy).not.toHaveBeenCalled();
    expect(result).toBe("shared");
  });

  it("fallback sur deps.copy quand share absent", async () => {
    const copy = vi.fn().mockResolvedValue(undefined);
    const result = await shareRecipe(
      { title: "Porc", url: "https://x/porc" },
      { share: undefined, copy },
    );
    expect(copy).toHaveBeenCalledWith("https://x/porc");
    expect(result).toBe("copied");
  });

  it("retourne 'cancelled' si share rejette avec AbortError", async () => {
    const err = Object.assign(new Error("user cancel"), { name: "AbortError" });
    const share = vi.fn().mockRejectedValue(err);
    const copy = vi.fn();
    const result = await shareRecipe(
      { title: "Porc", url: "https://x/porc" },
      { share, copy },
    );
    expect(result).toBe("cancelled");
    expect(copy).not.toHaveBeenCalled();
  });

  it("fallback sur copy si share rejette pour autre raison", async () => {
    const share = vi.fn().mockRejectedValue(new Error("nope"));
    const copy = vi.fn().mockResolvedValue(undefined);
    const result = await shareRecipe(
      { title: "Porc", url: "https://x/porc" },
      { share, copy },
    );
    expect(copy).toHaveBeenCalledWith("https://x/porc");
    expect(result).toBe("copied");
  });
});
