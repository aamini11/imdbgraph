import { defineConfig } from 'drizzle-kit'
import 'dotenv/config'
import { env } from './src/env.ts'

export default defineConfig({
	schema: './src/db/tables.ts',
	out: './src/db/migrations',
	dialect: 'postgresql',
	dbCredentials: {
		url: env.DATABASE_URL,
	},
})
