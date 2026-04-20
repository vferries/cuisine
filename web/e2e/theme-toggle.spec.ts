import { expect, test } from "@playwright/test";

test("toggle thème : bascule au clic et persiste au reload", async ({
  page,
}) => {
  await page.emulateMedia({ colorScheme: "light" });
  await page.goto("/");

  const bodyBg = () =>
    page.evaluate(() => getComputedStyle(document.body).backgroundColor);

  const initial = await bodyBg();

  await page.getByRole("button", { name: /thème/i }).click();
  const afterClick = await bodyBg();
  expect(afterClick).not.toBe(initial);

  await page.reload();
  expect(await bodyBg()).toBe(afterClick);
});
