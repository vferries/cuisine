import { expect, test } from "@playwright/test";

test("mode cuisson navigue étape par étape", async ({ page }) => {
  await page.goto("/cuisson/porc-bigorre-caramel");

  const progress = page.locator(".cuisson-progress");
  const activeStep = page.locator(".cuisson-step.is-active");

  await expect(progress).toContainText("1 /");
  await expect(activeStep).toContainText("carrés de 2");

  await page.getByRole("button", { name: "Suivant" }).click();

  await expect(progress).toContainText("2 /");
  await expect(activeStep).toContainText("dissoudre le sucre");
});
