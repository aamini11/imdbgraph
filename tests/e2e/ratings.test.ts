import { expect, test } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("ratings?id=tt0417299");
});

test("Screenshot Ratings Page", async ({ page }) => {
  await expect(page.locator("svg.highcharts-root")).toBeVisible();
  await expect(page).toHaveScreenshot({
    threshold: 0.5,
  });
});

test("Title works", async ({ page }) => {
  await expect(
    page.getByRole("heading", {
      name: "Avatar: The Last Airbender (2005 - 2008)",
    }),
  ).toBeVisible();
});
