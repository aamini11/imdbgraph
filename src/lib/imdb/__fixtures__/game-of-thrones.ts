import type { Ratings } from '@/lib/imdb/types'

export const gameOfThronesRatings: Ratings = {
	allEpisodeRatings: {
		1: {
			1: {
				episodeNum: 1,
				numVotes: 36939,
				rating: 9.1,
				seasonNum: 1,
				title: 'Winter Is Coming',
			},
			2: {
				episodeNum: 2,
				numVotes: 27976,
				rating: 8.8,
				seasonNum: 1,
				title: 'The Kingsroad',
			},
			3: {
				episodeNum: 3,
				numVotes: 26458,
				rating: 8.7,
				seasonNum: 1,
				title: 'Lord Snow',
			},
		},
		2: {
			1: {
				episodeNum: 1,
				numVotes: 23735,
				rating: 8.9,
				seasonNum: 2,
				title: 'The North Remembers',
			},
			2: {
				episodeNum: 2,
				numVotes: 22413,
				rating: 8.6,
				seasonNum: 2,
				title: 'The Night Lands',
			},
		},
	},
	show: {
		endYear: '2019',
		imdbId: 'tt0944947',
		numVotes: 1563413,
		rating: 9.4,
		startYear: '2011',
		title: 'Game of Thrones',
	},
}
