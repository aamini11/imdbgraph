import { test } from '@aamini/config/test/browser'
import { vi } from 'vite-plus/test'
import type { ComponentType } from 'react'
import type { Locator } from 'vite-plus/test/browser'
import type { RenderResult } from 'vitest-browser-react'

import { gameOfThronesRatings } from '@/lib/imdb/__fixtures__/game-of-thrones'
import { Route as HomeRoute } from '@/routes/index'
import { Route as RatingsRoute } from '@/routes/ratings/$id'

import { expectPageScreenshot } from './__mocks__/visual-page'

vi.mock('@/lib/imdb/ratings', () => ({
	getRatings: async () => gameOfThronesRatings,
}))

interface PageScreenshotCase {
	name: string
	path: string
	component: ComponentType
	waitFor: (screen: RenderResult) => Locator
	setup?: () => void | Promise<void>
}

const pages = [
	{
		name: 'home',
		path: '/',
		component: HomeRoute.options.component as ComponentType,
		waitFor: (screen) => screen.getByRole('heading', { name: /imdb graph/i }),
	},
	{
		name: 'ratings-game-of-thrones',
		path: '/ratings/$id',
		component: RatingsRoute.options.component as ComponentType,
		waitFor: (screen) =>
			screen.getByRole('heading', { name: /game of thrones/i }),
		setup: () => {
			;(
				RatingsRoute as unknown as { useLoaderData: () => unknown }
			).useLoaderData = () => gameOfThronesRatings
		},
	},
] satisfies PageScreenshotCase[]

test.each(pages)(
	'$name page matches desktop screenshot',
	async (visualPage) => {
		await expectPageScreenshot(visualPage)
	},
)
