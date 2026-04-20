import { expect, test } from "@playwright/test";

test("mode cuisson : le timer s'affiche en MM:SS et décompte après clic", async ({
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

  const beforeClick = await firstTimer.textContent();
  await page.waitForTimeout(1200);
  expect(await firstTimer.textContent()).toBe(beforeClick);

  await firstTimer.click();
  await page.waitForTimeout(1200);
  expect(await firstTimer.textContent()).not.toBe(beforeClick);
});
