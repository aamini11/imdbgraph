import { update } from '@/lib/imdb/scraper.ts'
import { drizzle } from 'drizzle-orm/node-postgres'
import { Pool } from 'pg'
import { env } from '../src/env.ts'

const pool = new Pool({
	connectionString: env.DATABASE_URL,
})

const db = drizzle({
	client: pool,
})

async function hasSeedData() {
	const result = await pool.query<{ has_rows: boolean }>(
		'SELECT EXISTS (SELECT 1 FROM "show" LIMIT 1) AS has_rows',
	)

	return result.rows[0]?.has_rows ?? false
}

async function bootstrapDb() {
	console.log('Checking seed data...')

	if (await hasSeedData()) {
		console.log('Seed data already exists. Skipping DB population.')
		return
	}

	console.log('No seed data found. Starting DB population...')
	await update(db)
}

try {
	await bootstrapDb()
	console.log('DB bootstrap completed successfully.')
} catch (e) {
	console.error('DB bootstrap failed:', e)
	process.exitCode = 1
} finally {
	await pool.end()
}
