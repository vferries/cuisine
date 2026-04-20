import { expect, test } from "@playwright/test";

test("augmenter les portions recalcule les quantités d'ingrédients", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");

  const portions = page.locator("#portions-value");
  const sucreQty = page
    .locator(".ingredient-list li")
    .filter({ hasText: "sucre" })
    .locator(".qty");

  await expect(portions).toHaveText("2");
  await expect(sucreQty).toHaveText("6 c. à c.");

  await page.getByRole("button", { name: "Augmenter" }).click();

  await expect(portions).toHaveText("3");
  await expect(sucreQty).toHaveText("9 c. à c.");
});
