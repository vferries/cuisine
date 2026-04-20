import { expect, test } from "@playwright/test";

test("dark mode : le background du body change avec prefers-color-scheme", async ({
  page,
}) => {
  const bodyBg = () =>
    page.evaluate(() => getComputedStyle(document.body).backgroundColor);

  await page.emulateMedia({ colorScheme: "light" });
  await page.goto("/");
  const light = await bodyBg();

  await page.emulateMedia({ colorScheme: "dark" });
  const dark = await bodyBg();

  expect(dark).not.toBe(light);
});
