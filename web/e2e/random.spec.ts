import { expect, test } from "@playwright/test";

test("Au hasard : navigue vers une recette du pool filtré par la recherche", async ({
  page,
}) => {
  await page.goto("/");

  // Filtre fort pour réduire la liste à un pool restreint et stable.
  await page.locator('input[type="search"]').fill("flan");

  const visibleSlugs = await page
    .locator(".recipe-list .recipe-row[data-slug]")
    .evaluateAll((rows) =>
      rows.map((r) => (r as HTMLElement).dataset.slug as string),
    );
  expect(visibleSlugs.length).toBeGreaterThan(0);

  await page.getByRole("button", { name: /au hasard/i }).click();

  await page.waitForURL(/\/[a-z0-9-]+$/);
  const url = page.url();
  const navigatedSlug = visibleSlugs.find((s) => url.endsWith(`/${s}`));
  expect(navigatedSlug).toBeDefined();
});
