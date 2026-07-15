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

describe("parseCook — somme multi-unités", () => {
  it("somme 500 g + 1 kg en 1500 g", () => {
    const src = [
      "== Test ==",
      "",
      "Saisir @porc{500%g}.",
      "",
      "Ajouter @porc{1%kg}.",
      "",
    ].join("\n");
    const r = parseCook(src);
    expect(r.ingredients).toEqual([
      { name: "porc", quantity: 1500, unit: "g" },
    ]);
  });

  it("somme 200 ml + 1 l en 1200 ml", () => {
    const src = "== Test ==\n\nVerser @lait{200%ml} puis @lait{1%l}.\n";
    const r = parseCook(src);
    expect(r.ingredients).toEqual([
      { name: "lait", quantity: 1200, unit: "ml" },
    ]);
  });

  it("normalise une occurrence unique en unité de base (1 kg → 1000 g)", () => {
    const r = parseCook("== Test ==\n\nPeser @farine{1%kg}.\n");
    expect(r.ingredients).toEqual([
      { name: "farine", quantity: 1000, unit: "g" },
    ]);
  });

  it("garde la 1ère occurrence quand les unités ne sont pas convertibles entre elles", () => {
    const src = "== Test ==\n\n@levure{1%kg} et @levure{1%sachet}.\n";
    const r = parseCook(src);
    expect(r.ingredients).toEqual([
      { name: "levure", quantity: 1000, unit: "g" },
    ]);
  });

  it("laisse les quantités texte inchangées", () => {
    const r = parseCook("== Test ==\n\nSaler @sel{au goût%g}.\n");
    expect(r.ingredients).toEqual([
      { name: "sel", quantity: "au goût", unit: "g" },
    ]);
  });

  it("ne touche pas aux tokens des étapes (l'auteur garde son unité)", () => {
    const r = parseCook("== Test ==\n\nPeser @farine{1%kg}.\n");
    const tok = r.sections[0].steps[0].tokens.find(
      (t) => t.type === "ingredient",
    );
    expect(tok).toEqual({
      type: "ingredient",
      ingredient: { name: "farine", quantity: 1, unit: "kg" },
    });
  });
});
