import { expect, test } from "@playwright/test";

test("tri 'Alphabétique' réordonne la liste par titre", async ({ page }) => {
  await page.goto("/");

  await page.getByRole("button", { name: /filtres avancés/i }).click();

  await page.getByRole("button", { name: "Alphabétique" }).click();

  const titles = await page.locator(".recipe-row .rec-title").allTextContents();
  const sorted = [...titles].sort((x, y) => x.localeCompare(y, "fr"));
  expect(titles).toEqual(sorted);
});

test("portée 'Ingrédients' restreint la recherche aux ingrédients", async ({
  page,
}) => {
  await page.goto("/");

  await page.getByRole("searchbox").fill("française");
  const visibleBefore = await page.locator(".recipe-row:visible").count();
  expect(visibleBefore).toBeGreaterThan(0);

  await page.getByRole("button", { name: /filtres avancés/i }).click();
  await page.getByRole("button", { name: "Ingrédients", exact: true }).click();

  await expect(page.locator(".recipe-row:visible")).toHaveCount(0);
});

test("le panneau de recherche avancée révèle un filtre course", async ({
  page,
}) => {
  await page.goto("/");

  const toggle = page.getByRole("button", { name: /filtres avancés/i });
  await expect(toggle).toBeVisible();

  const courseEntree = page.getByRole("button", { name: "Entrée", exact: true });
  await expect(courseEntree).toBeHidden();

  await toggle.click();
  await expect(courseEntree).toBeVisible();

  const porc = page.locator('.recipe-row[data-slug="porc-bigorre-caramel"]');
  const gaspacho = page.locator('.recipe-row[data-slug="gaspacho"]');
  await expect(porc).toBeVisible();
  await expect(gaspacho).toBeVisible();

  await courseEntree.click();
  await expect(porc).toBeHidden();
  await expect(gaspacho).toBeVisible();
});
