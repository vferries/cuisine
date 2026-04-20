import { expect, test } from "@playwright/test";

test("mode cuisson : le pill timer d'une étape affiche MM:SS statique, click alimente le tray", async ({
  page,
}) => {
  await page.goto("/cuisson/porc-bigorre-caramel");

  const timer = page.locator(".cuisson-step.is-active .step-timer");
  const next = page.getByRole("button", { name: "Suivant" });

  while ((await timer.count()) === 0) {
    await next.click();
  }

  const firstTimer = timer.first();
  await expect(firstTimer).toHaveText(/^\d+:[0-5]\d$/);

  const trayItems = page.locator(".timer-item");
  await expect(trayItems).toHaveCount(0);

  await firstTimer.click();
  await expect(trayItems).toHaveCount(1);
});
