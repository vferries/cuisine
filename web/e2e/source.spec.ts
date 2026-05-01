import { expect, test } from "@playwright/test";

test("source affichée sous le titre, en italique, préfixée par 'D'après'", async ({
  page,
}) => {
  await page.goto("/porc-bigorre-caramel");

  const source = page.locator(".recipe-source");
  await expect(source).toContainText(/^D[''']après /);
  await expect(source).toContainText(
    "Sarah Truong Qui, ancien chef de l'Empereur de Huê",
  );

  const fontStyle = await source.evaluate(
    (el) => window.getComputedStyle(el).fontStyle,
  );
  expect(fontStyle).toBe("italic");
});
