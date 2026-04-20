import { expect, test } from "@playwright/test";

test("le chip Végé filtre la liste, Toutes restaure", async ({ page }) => {
  await page.goto("/");

  const porc = page.locator('.recipe-row[data-slug="porc-bigorre-caramel"]');
  await expect(porc).toBeVisible();

  await page.getByRole("button", { name: "Végé" }).click();
  await expect(porc).toBeHidden();

  await page.getByRole("button", { name: "Toutes" }).click();
  await expect(porc).toBeVisible();
});
