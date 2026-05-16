import { test } from '@aamini/config/test/browser'
import { gameOfThronesRatings } from '@/lib/imdb/__fixtures__/game-of-thrones'
import { render } from 'vitest-browser-react'
import { expect } from 'vite-plus/test'
import { Graph } from './graph'

test('graph displays show title', async () => {
	const screen = await render(
		<div className="dark bg-background min-h-screen p-6">
			<Graph ratings={gameOfThronesRatings} />
		</div>,
	)

	await expect
		.element(screen.getByRole('heading', { name: 'Game of Thrones' }))
		.toBeVisible()
})
