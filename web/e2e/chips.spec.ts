import { expect, test } from "@playwright/test";

test("le chip Végé filtre la liste, Toutes restaure", async ({ page }) => {
  await page.goto("/");

  const rows = page.locator(".recipe-row");
  await expect(rows).toHaveCount(1);

  await page.getByRole("button", { name: "Végé" }).click();
  await expect(rows).toHaveCount(0);

  await page.getByRole("button", { name: "Toutes" }).click();
  await expect(rows).toHaveCount(1);
});
