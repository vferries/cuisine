import { expect, test } from "@playwright/test";

test("le bouton 'Mode cuisson' navigue vers /cuisson/[slug]", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");

  await page.getByRole("link", { name: /Mode cuisson/i }).click();

  await expect(page).toHaveURL(/\/cuisson\/porc-bigorre-caramel$/);
});
