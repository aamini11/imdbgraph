import { Navbar } from '@/components/navbar'
import { SearchBar } from '@/components/search-bar'
import { Card } from '@/components/ui/card'
import { createFileRoute } from '@tanstack/react-router'
import { Sparkles, Star } from 'lucide-react'

export const Route = createFileRoute('/')({
	component: Home,
})

const featuredShows = [
	{
		imdbId: 'tt0944947',
		title: 'Game of Thrones',
		years: '2011-2019',
		rating: 9.2,
		genre: 'Fantasy',
		trend: '+18%',
		posterUrl:
			'https://media.themoviedb.org/t/p/w300_and_h450_face/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg',
	},
	{
		imdbId: 'tt0903747',
		title: 'Breaking Bad',
		years: '2008-2013',
		rating: 9.5,
		genre: 'Crime',
		trend: '+12%',
		posterUrl:
			'https://media.themoviedb.org/t/p/w300_and_h450_face/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg',
	},
	{
		imdbId: 'tt4574334',
		title: 'Stranger Things',
		years: '2016-2025',
		rating: 8.7,
		genre: 'Sci-Fi',
		trend: '+31%',
		posterUrl:
			'https://media.themoviedb.org/t/p/w300_and_h450_face/uOOtwVbSr4QDjAGIifLDwpb2Pdl.jpg',
	},
	{
		imdbId: 'tt0141842',
		title: 'The Sopranos',
		years: '1999-2007',
		rating: 9.2,
		genre: 'Drama',
		trend: '+8%',
		posterUrl:
			'https://media.themoviedb.org/t/p/w300_and_h450_face/rTc7ZXdroqjkKivFPvCPX0Ru7uw.jpg',
	},
]

function Home() {
	return (
		<div className="flex flex-1 flex-col">
			<Navbar />
			<main className="flex flex-1 flex-col">
				<section>
					<div className="mx-auto max-w-4xl px-4 py-14 md:px-6 md:py-24">
						<h1 className="text-center text-5xl font-black tracking-tight text-balance">
							IMDbGraph.org
						</h1>
						<p className="text-muted-foreground text-center text-sm leading-7 not-first:mt-3">
							Episode ratings for every TV series.
						</p>

						<SearchBar className="mx-auto mt-10 max-w-xl" />
					</div>
				</section>

				<section className="flex-1 px-4 pb-8 md:px-6 md:py-20">
					<div className="mx-auto max-w-5xl">
						<div className="mb-8 flex items-center gap-3">
							<Sparkles className="text-primary h-5 w-5" />
							<h2 className="text-xl font-bold tracking-tight">Trending Now</h2>
						</div>
						<div className="grid gap-4 sm:grid-cols-2">
							{featuredShows.map((show) => (
								<FeaturedShowCard key={show.imdbId} show={show} />
							))}
						</div>
					</div>
				</section>
			</main>
		</div>
	)
}

function FeaturedShowCard({ show }: { show: (typeof featuredShows)[number] }) {
	return (
		<Card className="p-4">
			<div className="flex gap-4">
				<img
					src={show.posterUrl}
					alt={show.title}
					className="aspect-2/3 w-20 shrink-0 border object-cover"
				/>
				<div className="min-w-0 flex-1">
					<div className="flex items-center justify-between gap-3">
						<span className="bg-secondary text-secondary-foreground border px-2 py-0.5 text-xs font-black">
							{show.genre}
						</span>
						<span className="flex items-center gap-1 text-sm font-black">
							<Star className="text-primary h-4 w-4 fill-current" />{' '}
							{show.rating}
						</span>
					</div>
					<h3 className="group-hover:text-primary mt-3 truncate text-lg font-bold tracking-tight">
						{show.title}
					</h3>
					<p className="text-muted-foreground mt-1 text-sm font-bold">
						{show.years}
					</p>
				</div>
			</div>
		</Card>
	)
}
