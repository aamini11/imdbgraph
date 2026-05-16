import { test } from '@aamini/config/test/db'
import { downloadStream, type ImdbFile } from '@/lib/imdb/file-downloader'
import { getRatingsDb } from '@/lib/imdb/ratings'
import { update } from '@/lib/imdb/scraper'
import { createReadStream } from 'node:fs'
import path from 'node:path'
import { describe, expect, vi } from 'vite-plus/test'
import { gameOfThronesRatings } from './__fixtures__/game-of-thrones.ts'

vi.mock(import('@/lib/imdb/file-downloader'))

const GAME_OF_THRONES_ID = 'tt0944947'
const SIMPSONS_ID = 'tt0096697'

// =============================================================================
// Tests
// =============================================================================
describe('scraper tests', () => {
	test('loading sample files into database', async ({ db }) => {
		mockDownloads({
			'title.basics.tsv.gz': './__fixtures__/titles.tsv',
			'title.episode.tsv.gz': './__fixtures__/episodes.tsv',
			'title.ratings.tsv.gz': './__fixtures__/ratings.tsv',
		})

		await update(db)

		expect(await getRatingsDb(db, GAME_OF_THRONES_ID)).toEqual(
			gameOfThronesRatings,
		)
		expect(await getRatingsDb(db, SIMPSONS_ID)).toBeUndefined()
	})

	test('handling bad files', async ({ db }) => {
		mockDownloads({
			'title.basics.tsv.gz': './__fixtures__/titles.tsv',
			'title.episode.tsv.gz': './__fixtures__/bad-episodes.tsv',
			'title.ratings.tsv.gz': './__fixtures__/ratings.tsv',
		})
		await expect(update(db)).rejects.toThrow(
			'invalid input syntax for type integer: "5   corrupt-data 1212"',
		)
	})
})

// =============================================================================
// Helpers
// =============================================================================
function mockDownloads(mockedFiles: Record<ImdbFile, string>) {
	vi.mocked(downloadStream).mockImplementation(async (imdbFile) => {
		const input = path.join(import.meta.dirname, mockedFiles[imdbFile])
		return createReadStream(input)
	})
}
