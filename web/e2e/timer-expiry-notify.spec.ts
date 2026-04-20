import { expect, test } from "@playwright/test";

test("timer expiré : son joué une fois + classe d'animation posée", async ({
  page,
}) => {
  await page.addInitScript(() => {
    (window as any).__audioPlays = 0;
    const origPlay = HTMLAudioElement.prototype.play;
    HTMLAudioElement.prototype.play = function () {
      (window as any).__audioPlays++;
      try {
        return origPlay.apply(this);
      } catch {
        return Promise.resolve();
      }
    };
  });
  await page.clock.install();
  await page.goto("/porc-bigorre-caramel");

  await page.locator(".step-timer[data-seconds]").first().click();

  // Avant expiration : pas de son, pas d'is-expired
  await expect(page.locator(".timer-item.is-expired")).toHaveCount(0);
  expect(await page.evaluate(() => (window as any).__audioPlays)).toBe(0);

  // Avance au-delà de la durée du timer (caramel 3 min)
  await page.clock.fastForward(4 * 60 * 1000);

  await expect(page.locator(".timer-item.is-expired")).toHaveCount(1);
  const plays = await page.evaluate(() => (window as any).__audioPlays);
  expect(plays).toBeGreaterThanOrEqual(1);

  // Pas de replay incessant : encore 3 secondes et le compteur doit rester stable
  await page.clock.fastForward(3000);
  const playsAfter = await page.evaluate(() => (window as any).__audioPlays);
  expect(playsAfter).toBe(plays);
});
