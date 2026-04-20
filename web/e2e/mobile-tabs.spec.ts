import { expect, test } from "@playwright/test";

test("vue recette mobile : onglets Ingrédients / Étapes / Ustensiles basculent le contenu", async ({
  page,
}) => {
  await page.setViewportSize({ width: 375, height: 667 });
  await page.goto("/porc-bigorre-caramel");

  const ingredients = page.locator(".ingredient-list");
  const firstStep = page.locator(".section-heading").first();
  const cookware = page.locator(".cookware-list");

  await expect(ingredients).toBeVisible();
  await expect(firstStep).toBeHidden();
  await expect(cookware).toBeHidden();

  await page.getByRole("tab", { name: "Étapes" }).click();
  await expect(firstStep).toBeVisible();
  await expect(ingredients).toBeHidden();

  await page.getByRole("tab", { name: "Ustensiles" }).click();
  await expect(cookware).toBeVisible();
  await expect(firstStep).toBeHidden();
});
