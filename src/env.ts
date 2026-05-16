import { config } from 'dotenv'
import { z } from 'zod'

const environmentName = (() => {
	const raw =
		process.env.RAILWAY_ENVIRONMENT_NAME ??
		process.env.NODE_ENV ??
		'development'

	return /(?:^|-)pr-\d+$/.test(raw) ? 'staging' : raw
})()

const fileEnvironment: NodeJS.ProcessEnv = {}

config({
	path: [
		'.env',
		'.env.local',
		`.env.${environmentName}`,
		`.env.${environmentName}.local`,
	],
	quiet: true,
	override: true,
	processEnv: fileEnvironment,
})

for (const [key, value] of Object.entries(fileEnvironment)) {
	process.env[key] ??= value
}

export const env = z
	.object({
		DATABASE_URL: z.string(),
	})
	.parse(process.env)
