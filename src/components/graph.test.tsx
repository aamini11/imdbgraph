import { test } from '@aamini/config/test/browser'
import { expect, vi } from 'vite-plus/test'
import { gameOfThronesRatings } from '@/lib/imdb/__fixtures__/game-of-thrones'
import { Route as RatingsRoute } from '@/routes/ratings/$id'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
	RouterContextProvider,
	createRootRoute,
	createRoute,
	createRouter,
} from '@tanstack/react-router'
import { page } from 'vite-plus/test/browser'
import { render } from 'vitest-browser-react'
import type { ComponentType, ReactNode } from 'react'

vi.mock('@/lib/imdb/ratings', () => ({
	getRatings: async () => gameOfThronesRatings,
}))

const testQueryClient = new QueryClient({
	defaultOptions: {
		queries: {
			retry: false,
		},
	},
})

function createMockRouter(path: string, component: ComponentType) {
	const rootRoute = createRootRoute()
	const testRoute = createRoute({
		getParentRoute: () => rootRoute,
		path,
		component: () => {
			const Component = component
			return <Component />
		},
	})
	return createRouter({
		routeTree: rootRoute.addChildren([testRoute]),
	})
}

function createWrapper(path: string, component: ComponentType) {
	return function MockRouter({ children }: { children: ReactNode }) {
		return (
			<QueryClientProvider client={testQueryClient}>
				<RouterContextProvider router={createMockRouter(path, component)}>
					{children}
				</RouterContextProvider>
			</QueryClientProvider>
		)
	}
}

test('ratings page screenshot', async () => {
	await page.viewport(1280, 900)
	;(RatingsRoute as unknown as { useLoaderData: () => unknown }).useLoaderData =
		() => gameOfThronesRatings

	const Component = RatingsRoute.options.component as ComponentType
	const screen = await render(
		<div className="dark bg-background min-h-screen w-screen">
			<Component />
		</div>,
		{ wrapper: createWrapper('/ratings/$id', Component) },
	)

	await expect
		.element(screen.getByRole('heading', { name: 'Game of Thrones' }))
		.toBeVisible()
	await expect.element(page.getByTestId('ratings-graph')).toBeVisible()
	await expect.element(screen.container).toMatchScreenshot('ratings-page.png')
})
