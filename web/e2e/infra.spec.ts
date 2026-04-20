import { expect, test } from "@playwright/test";

test("accueil répond avec le titre attendu", async ({ page }) => {
  await page.goto("/");
  await expect(page).toHaveTitle("Mes recettes");
});
