import { describe, expect, it } from "vitest";
import { planImages } from "./image-plan.ts";

describe("planImages", () => {
  it("retourne un plan main + thumb pour chaque image source", () => {
    const plans = planImages(["porc.png"]);
    expect(plans).toEqual([
      {
        source: "porc.png",
        slug: "porc",
        outputs: [
          { filename: "porc.webp", width: 1024, height: 768 },
          { filename: "porc.thumb.webp", width: 320, height: 240 },
        ],
      },
    ]);
  });

  it("accepte png, jpg, jpeg, webp en source (case-insensitive)", () => {
    const plans = planImages(["a.png", "b.jpg", "c.JPEG", "d.webp"]);
    expect(plans.map((p) => p.slug)).toEqual(["a", "b", "c", "d"]);
  });

  it("ignore les fichiers non-image", () => {
    const plans = planImages(["porc.png", ".gitkeep", "notes.txt"]);
    expect(plans.map((p) => p.slug)).toEqual(["porc"]);
  });
});
