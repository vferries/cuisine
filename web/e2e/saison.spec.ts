import { expect, test } from "@playwright/test";

test("la chip De saison filtre sans casser la liste", async ({ page }) => {
  await page.goto("/");

  const rows = page.locator(".recipe-row:visible");
  const total = await rows.count();
  expect(total).toBeGreaterThan(0);

  await page.getByRole("button", { name: "De saison" }).click();
  expect(await rows.count()).toBeLessThanOrEqual(total);

  await page.getByRole("button", { name: "Toutes" }).click();
  await expect(rows).toHaveCount(total);
});
