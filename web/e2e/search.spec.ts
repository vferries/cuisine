import { expect, test } from "@playwright/test";

test("la recherche filtre la liste des recettes", async ({ page }) => {
  await page.goto("/");

  const porc = page.locator('.recipe-row[data-slug="porc-bigorre-caramel"]');
  await expect(porc).toBeVisible();

  const search = page.getByRole("searchbox");

  await search.fill("xyzzyinconnu");
  await expect(porc).toBeHidden();

  await search.fill("porc");
  await expect(porc).toBeVisible();
});
