import { expect, test } from "@playwright/test";

test("pluralisation : l'unité s'accorde avec la qty scalée", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");

  const ciboule = page
    .locator(".ingredient-list li")
    .filter({ hasText: "ciboule" })
    .locator(".qty");

  await expect(ciboule).toHaveText("2 brins");

  await page.getByRole("button", { name: "Diminuer" }).click();
  await expect(ciboule).toHaveText("1 brin");
});

test("pluralisation ustensiles : nom accordé quand qty > 1", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");
  const cookware = page.locator(".cookware-list");

  await expect(cookware).toContainText("2 poêles");
});
