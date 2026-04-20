import { describe, expect, it } from "vitest";
import { matchingRecipeSlugs } from "./search";

const porc = {
  slug: "porc-bigorre-caramel",
  title: "Porc noir de Bigorre confit au caramel",
  cuisine: "vietnamienne",
  tags: ["porc", "asiatique"],
  ingredientNames: ["sucre", "nuoc-mâm", "piment"],
};

const risotto = {
  slug: "risotto-champignons",
  title: "Risotto aux champignons",
  cuisine: "italienne",
  tags: ["végé"],
  ingredientNames: ["riz arborio", "champignons"],
};

describe("matchingRecipeSlugs", () => {
  it("retourne le slug d'une recette dont le titre matche la requête", () => {
    const slugs = matchingRecipeSlugs([porc, risotto], "porc");
    expect(slugs).toEqual(["porc-bigorre-caramel"]);
  });

  it("retourne un tableau vide quand aucune recette ne matche", () => {
    const slugs = matchingRecipeSlugs([porc, risotto], "xyzzyinconnu");
    expect(slugs).toEqual([]);
  });

  it("retourne tous les slugs quand la requête est vide", () => {
    const slugs = matchingRecipeSlugs([porc, risotto], "");
    expect(slugs).toEqual(["porc-bigorre-caramel", "risotto-champignons"]);
  });
});
