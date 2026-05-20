import { createAppConfig } from '@aamini/config'
import { mergeConfig } from 'vite-plus'
import { playwright } from 'vite-plus/test/browser-playwright'

const root = new URL('.', import.meta.url).pathname

export default mergeConfig(
	createAppConfig(root, {
		browser: {
			test: {
				browser: {
					provider: playwright(),
				},
			},
		},
	}),
	{
		test: {
			deps: {
				optimizer: {
					client: {
						enabled: true,
						include: ['aria-query'],
					},
				},
			},
		},
		ssr: {
			noExternal: ['recharts', '@aamini/config'],
		},
	},
)
