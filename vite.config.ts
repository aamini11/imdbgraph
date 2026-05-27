import { createAppConfig } from '@aamini/config/vite'
import { mergeConfig } from 'vite-plus'
import { playwright } from 'vite-plus/test/browser-playwright'

const root = new URL('.', import.meta.url).pathname

export default mergeConfig(
	createAppConfig({
		root,
		projectOverrides: {
			browser: {
				test: {
					browser: {
						provider: playwright({
							launchOptions: {
								args: ['--disable-lcd-text'],
							},
						}),
					},
				},
			},
		},
	}),
	{
		lint: {
			overrides: [
				{
					files: [
						'scripts/**',
						'src/lib/imdb/file-downloader.ts',
						'src/lib/imdb/scraper.ts',
					],
					rules: {
						'no-console': 'off',
					},
				},
			],
		},
		ssr: {
			noExternal: ['recharts', '@aamini/config'],
		},
	},
)
