import { expect, test } from "@playwright/test";

test("partage natif : navigator.share appelé avec titre + URL de la page", async ({
  page,
}) => {
  await page.addInitScript(() => {
    (window as any).__shareCalls = [];
    (navigator as any).share = (data: any) => {
      (window as any).__shareCalls.push(data);
      return Promise.resolve();
    };
  });

  await page.goto("/porc-bigorre-caramel");

  await page.getByRole("button", { name: /Partager/i }).click();

  const calls = await page.evaluate(() => (window as any).__shareCalls);
  expect(calls).toHaveLength(1);
  expect(calls[0].title).toMatch(/Porc/i);
  expect(calls[0].url).toContain("/porc-bigorre-caramel");
});

test("partage fallback : copie l'URL dans le presse-papier si navigator.share absent", async ({
  page,
}) => {
  await page.addInitScript(() => {
    delete (navigator as any).share;
    (window as any).__copied = [];
    Object.defineProperty(navigator, "clipboard", {
      configurable: true,
      value: {
        writeText: (t: string) => {
          (window as any).__copied.push(t);
          return Promise.resolve();
        },
      },
    });
  });

  await page.goto("/porc-bigorre-caramel");

  await page.getByRole("button", { name: /Partager/i }).click();

  const copied = await page.evaluate(() => (window as any).__copied);
  expect(copied).toHaveLength(1);
  expect(copied[0]).toContain("/porc-bigorre-caramel");

  await expect(page.getByRole("button", { name: /Partager/i })).toContainText(
    /Lien copié/i,
  );
});
