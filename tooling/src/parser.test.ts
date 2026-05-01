import { describe, expect, it } from "vitest";
import { parseCook } from "./parser";

describe("parseCook — comments", () => {
  it("ignore un commentaire -- inline (mi-ligne) et garde le reste de la step", () => {
    const src = [
      "== Test ==",
      "",
      "Ajouter @sucre{2%càc} -- mais avec parcimonie",
      "",
    ].join("\n");
    const r = parseCook(src);
    expect(r.sections).toHaveLength(1);
    expect(r.sections[0].steps).toHaveLength(1);
    const text = r.sections[0].steps[0].tokens
      .map((t) => (t.type === "text" ? t.text : ""))
      .join("");
    expect(text).not.toMatch(/parcimonie/);
    expect(r.ingredients.find((i) => i.name === "sucre")).toBeDefined();
  });

  it("ignore un bloc [- ... -] sur une ligne et conserve le contexte autour", () => {
    const src = [
      "== Test ==",
      "",
      "Ajouter [- TODO: vérifier la quantité -] @sel{1%càc} dans la poêle.",
      "",
    ].join("\n");
    const r = parseCook(src);
    const text = r.sections[0].steps[0].tokens
      .map((t) => (t.type === "text" ? t.text : "") + (t.type === "ingredient" ? t.ingredient.name : ""))
      .join("");
    expect(text).not.toMatch(/TODO/);
    expect(text).toMatch(/Ajouter\s+sel\s+dans la poêle/);
  });

  it("ignore un bloc [- ... -] qui s'étend sur plusieurs lignes", () => {
    const src = [
      "== Test ==",
      "",
      "Première étape avec @ail{1}.",
      "",
      "[- bloc note multi-ligne",
      "qui ne devrait pas être rendu",
      "comme une étape -]",
      "",
      "Deuxième étape avec @oignon{1}.",
      "",
    ].join("\n");
    const r = parseCook(src);
    expect(r.sections[0].steps).toHaveLength(2);
    const allText = r.sections[0].steps
      .flatMap((s) => s.tokens.map((t) => (t.type === "text" ? t.text : "")))
      .join(" ");
    expect(allText).not.toMatch(/bloc note/);
    expect(allText).not.toMatch(/multi-ligne/);
    expect(r.ingredients.map((i) => i.name).sort()).toEqual(["ail", "oignon"]);
  });
});
