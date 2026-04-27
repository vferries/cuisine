import { expect, test } from "@playwright/test";

test("favoris : marquage depuis le détail, chip Favoris filtre la liste", async ({
  page,
}) => {
  await page.goto("/");
  await page.evaluate(() => localStorage.clear());

  await page.goto("/porc-bigorre-caramel");

  const heart = page.getByRole("button", { name: /favori/i });
  await expect(heart).toHaveAttribute("aria-pressed", "false");

  await heart.click();
  await expect(heart).toHaveAttribute("aria-pressed", "true");

  await page.reload();
  await expect(heart).toHaveAttribute("aria-pressed", "true");

  await page.goto("/");
  const porc = page.locator('.recipe-row[data-slug="porc-bigorre-caramel"]');
  const katsu = page.locator('.recipe-row[data-slug="katsudon"]');
  await expect(porc).toBeVisible();
  await expect(katsu).toBeVisible();

  await page.getByRole("button", { name: "Favoris" }).click();
  await expect(porc).toBeVisible();
  await expect(katsu).toBeHidden();
});
