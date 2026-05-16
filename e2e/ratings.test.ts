import { expect, test } from '@playwright/test'

test.beforeEach(async ({ page }) => {
	await page.goto('/')
})

test('Ratings graph renders', async ({ page }) => {
	await page.goto('/ratings/tt0944947')
	await expect(page).toHaveURL(/.*\/ratings\/tt0944947/)
	await expect(page.getByTestId('ratings-graph')).toBeVisible()
	await expect(
		page.getByRole('heading', { name: 'Game of Thrones' }),
	).toBeVisible()
	await expect(
		page.getByText('Rating: 9.4 / 10.0 (1,563,413 votes)'),
	).toBeVisible()
})
