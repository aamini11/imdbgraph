import { SearchBar } from '@/components/search-bar'
import { createFileRoute, useRouter } from '@tanstack/react-router'
import { useEffect } from 'react'

export const Route = createFileRoute('/')({
	component: Home,
})

function Home() {
	const router = useRouter()

	useEffect(() => {
		// Preload the ratings page code bundle (without executing the loader)
		const ratingsRoute = router.routesByPath['/ratings/$id']
		const loadRouteChunk = router.loadRouteChunk
		if (ratingsRoute && loadRouteChunk) {
			loadRouteChunk(ratingsRoute)?.catch(() => {
				// Silently fail if preload doesn't work
			})
		}
	}, [router])

	return (
		<div className="flex flex-1 flex-col items-center gap-6 pt-8 md:gap-9 md:pt-20">
			<h1 className="inline px-8 text-center text-4xl font-semibold tracking-tight text-balance md:text-5xl lg:text-6xl">
				IMDb Graph
			</h1>
			<div className="flex w-full max-w-lg min-w-sm justify-center px-8">
				<SearchBar />
			</div>
		</div>
	)
}
