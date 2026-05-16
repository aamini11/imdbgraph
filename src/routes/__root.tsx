/// <reference types="vite-plus/client" />
import type { QueryClient } from '@tanstack/react-query'
import {
	ClientOnly,
	HeadContent,
	Outlet,
	Scripts,
	createRootRouteWithContext,
} from '@tanstack/react-router'
import { useEffect } from 'react'
import posthog from 'posthog-js'
import appCss from '../styles.css?url'
function Analytics() {
	useEffect(() => {
		const posthogKey = import.meta.env.VITE_PUBLIC_POSTHOG_KEY
		if (import.meta.env.MODE === 'development' || !posthogKey) return

		posthog.init(posthogKey, {
			api_host: '/api/ingest',
			ui_host: 'https://us.posthog.com',
			defaults: '2025-11-30',
			person_profiles: 'always',
		})
	}, [])

	return null
}

export const Route = createRootRouteWithContext<{
	queryClient: QueryClient
}>()({
	head: () => ({
		meta: [
			{
				charSet: 'utf-8',
			},
			{
				name: 'viewport',
				content: 'width=device-width, initial-scale=1',
			},
			{
				title: 'IMDB Graph',
			},
		],
		links: [{ rel: 'stylesheet', href: appCss }],
	}),
	component: RootComponent,
})

function RootComponent() {
	return (
		<html lang="en">
			<head>
				<HeadContent />
			</head>
			<body className="bg-background dark h-dvh min-w-80 antialiased">
				<div className="flex min-h-dvh flex-col">
					{/* Main content */}
					<div className="flex-1">
						<Outlet />
					</div>
					{/* Footer */}
					<footer className="w-full p-2 text-center text-xs">
						<span className="text-muted-foreground leading-loose text-balance">
							Built by{' '}
							<a
								href="https://www.linkedin.com/in/aria-amini/"
								target="_blank"
								rel="noreferrer"
								className="font-medium underline underline-offset-4"
							>
								Aria
							</a>
							. The source code is available on{' '}
							<a
								href="https://github.com/aamini11/imdbgraph"
								target="_blank"
								rel="noreferrer"
								className="font-medium underline underline-offset-4"
							>
								GitHub
							</a>
							.
						</span>
					</footer>
				</div>
				<ClientOnly fallback={null}>
					<Analytics />
				</ClientOnly>
				<Scripts />
			</body>
		</html>
	)
}
