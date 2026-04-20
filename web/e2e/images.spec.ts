import { expect, test } from "@playwright/test";

test("accueil : la vignette de chaque recette pointe la thumb générée", async ({
  page,
}) => {
  await page.goto("/");
  const thumb = page.locator(
    '.recipe-row[data-slug="porc-bigorre-caramel"] img',
  );
  await expect(thumb).toHaveAttribute(
    "src",
    /\/images\/porc-bigorre-caramel\.thumb\.webp$/,
  );
});

test("vue recette : l'image héro pointe la version 1024×768", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");
  const hero = page.locator(".hero-thumb img");
  await expect(hero).toHaveAttribute(
    "src",
    /\/images\/porc-bigorre-caramel\.webp$/,
  );
});
