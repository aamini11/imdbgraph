import { createEnv } from '@aamini/lib/env'
import { z } from 'zod'

export const env = createEnv(
	z.object({
		DATABASE_URL: z.string(),
	}),
)
