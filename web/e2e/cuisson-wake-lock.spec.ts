import { expect, test } from "@playwright/test";

test("mode cuisson : demande un Wake Lock écran au chargement", async ({
  page,
}) => {
  await page.addInitScript(() => {
    (window as any).__wakeLockCalls = [];
    Object.defineProperty(navigator, "wakeLock", {
      configurable: true,
      value: {
        request: async (type: string) => {
          (window as any).__wakeLockCalls.push(type);
          return {
            released: false,
            release: async function () {
              (this as any).released = true;
            },
            type,
            addEventListener: () => {},
          };
        },
      },
    });
  });

  await page.goto("/cuisson/porc-bigorre-caramel");

  await page.waitForFunction(
    () => (window as any).__wakeLockCalls.length > 0,
    null,
    { timeout: 3000 },
  );

  const calls = await page.evaluate(() => (window as any).__wakeLockCalls);
  expect(calls).toEqual(["screen"]);
});
