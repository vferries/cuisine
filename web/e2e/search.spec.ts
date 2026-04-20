import { expect, test } from "@playwright/test";

test("la recherche filtre la liste des recettes", async ({ page }) => {
  await page.goto("/");

  const rows = page.locator(".recipe-row");
  await expect(rows).toHaveCount(1);

  const search = page.getByRole("searchbox");

  await search.fill("xyzzyinconnu");
  await expect(rows).toHaveCount(0);

  await search.fill("porc");
  await expect(rows).toHaveCount(1);
});
