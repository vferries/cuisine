import { expect, test } from "@playwright/test";

test("impression : chrome masqué, tous les panels visibles malgré l'onglet mobile", async ({
  page,
}) => {
  // Viewport mobile : par défaut seul le panel Ingrédients est visible.
  await page.setViewportSize({ width: 375, height: 800 });
  await page.goto("/porc-bigorre-caramel");

  await expect(
    page.locator('.panel[data-panel="steps"]').first(),
  ).toBeHidden();

  await page.emulateMedia({ media: "print" });

  await expect(page.locator(".back-link")).toBeHidden();
  await expect(page.locator(".header-actions")).toBeHidden();
  await expect(page.locator(".theme-toggle")).toBeHidden();
  await expect(page.locator(".tabs")).toBeHidden();
  await expect(page.locator(".portions")).toBeHidden();

  await expect(
    page.locator('.panel[data-panel="ingredients"]').first(),
  ).toBeVisible();
  await expect(
    page.locator('.panel[data-panel="steps"]').first(),
  ).toBeVisible();
  await expect(
    page.locator('.panel[data-panel="cookware"]').first(),
  ).toBeVisible();

  await expect(page.locator(".recipe-header h1")).toBeVisible();
  await expect(page.locator(".recipe-source")).toBeVisible();
});
