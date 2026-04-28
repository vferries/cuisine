import { expect, test } from "@playwright/test";

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
