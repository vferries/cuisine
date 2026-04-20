import { expect, test } from "@playwright/test";

test("timer tray : click démarre, stack, persiste cross-page, expire, dismiss", async ({
  page,
}) => {
  await page.clock.install();
  await page.goto("/porc-bigorre-caramel");

  const tray = page.locator(".timer-tray");
  const items = tray.locator(".timer-item");

  await expect(tray).toBeHidden();

  const pills = page.locator(".step-timer[data-seconds]");
  await pills.first().click(); // 1er : caramel 3 min

  await expect(tray).toBeVisible();
  await expect(items).toHaveCount(1);

  // Stack : un 2e timer long (mijotage 15 min) pour distinguer l'expiration
  await pills.nth(2).click();
  await expect(items).toHaveCount(2);

  // Persistance cross-page (navigation vers accueil)
  await page.goto("/");
  await expect(page.locator(".timer-tray")).toBeVisible();
  await expect(page.locator(".timer-item")).toHaveCount(2);

  // Avance du temps pour expirer (> 3 min)
  await page.clock.fastForward(4 * 60 * 1000);
  await expect(page.locator(".timer-item.is-expired")).toHaveCount(1);

  // Dismiss l'expiré
  await page
    .locator(".timer-item.is-expired")
    .locator('button[aria-label*="Arrêter"]')
    .click();
  await expect(page.locator(".timer-item")).toHaveCount(1);

  // Dismiss le second, tray disparaît
  await page
    .locator(".timer-item")
    .locator('button[aria-label*="Arrêter"]')
    .click();
  await expect(page.locator(".timer-tray")).toBeHidden();
});
