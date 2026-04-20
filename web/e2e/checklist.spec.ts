import { expect, test } from "@playwright/test";

test("ingrédients : cocher/décocher, persistance, bouton tout décocher", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");

  const sucre = page
    .locator(".ingredient-list li")
    .filter({ hasText: "sucre" });

  await expect(sucre).not.toHaveClass(/is-checked/);

  await sucre.click();
  await expect(sucre).toHaveClass(/is-checked/);

  await page.reload();
  await expect(sucre).toHaveClass(/is-checked/);

  await page.getByRole("button", { name: /Tout décocher/i }).click();
  await expect(sucre).not.toHaveClass(/is-checked/);

  await page.reload();
  await expect(sucre).not.toHaveClass(/is-checked/);
});
