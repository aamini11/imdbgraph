'use client'

import { type Episode, type Ratings } from '@/lib/imdb/types'
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from '@aamini/ui/components/card'
import {
	type ChartConfig,
	ChartContainer,
	ChartTooltip,
} from '@aamini/ui/components/chart'
import {
	CartesianGrid,
	Line,
	LineChart,
	type TooltipProps,
	XAxis,
	YAxis,
} from 'recharts'

interface ChartDataPoint {
	episodeIndex: number
	[key: string]: number | Episode | null
}

export function Graph({ ratings }: { ratings: Ratings }) {
	const { show } = ratings
	const { data: chartData, seasons } = transformRatingsData(ratings)
	const chartConfig: ChartConfig = {}
	const chartColors = [
		'var(--chart-1)',
		'var(--chart-2)',
		'var(--chart-3)',
		'var(--chart-4)',
		'var(--chart-5)',
	]

	seasons.forEach((seasonNum, index) => {
		chartConfig[`season${seasonNum}`] = {
			label: `Season ${seasonNum}`,
			color: chartColors[index % chartColors.length] ?? 'var(--chart-1)',
		}
	})

	// min()
	return (
		<Card
			data-testid="ratings-graph"
			className="m-[clamp(16px,(100vw-340px)*0.09,30px)] min-h-[300px] flex-1 px-[clamp(0px,(100vw-340px)*0.09,30px)] py-6"
		>
			<CardHeader className="text-center">
				<h1 className="text-xl leading-none font-extrabold tracking-tight text-balance">
					{show.title}
				</h1>
				<span className="text-muted-foreground text-sm">
					Rating: {show.rating.toFixed(1)} / 10.0 (
					{show.numVotes.toLocaleString()} votes)
				</span>
			</CardHeader>
			<CardContent className="px-0">
				<ChartContainer config={chartConfig}>
					<LineChart
						accessibilityLayer
						data={chartData}
						margin={{
							left: 12,
							right: 12,
						}}
					>
						<CartesianGrid vertical={false} />
						<XAxis
							dataKey="episodeIndex"
							tick={false}
							domain={[0, 'dataMax']}
						/>
						<YAxis
							tickLine={true}
							axisLine={true}
							tick={true}
							allowDecimals={false}
							width={20} // Fixes bug where there's too much margin on left.
							domain={[(dataMin: number) => Math.floor(dataMin), 10.0]}
						/>
						<ChartTooltip content={<CustomTooltip />} />
						{seasons.map((seasonNum) => (
							<Line
								key={seasonNum}
								dataKey={`season${seasonNum}`}
								type="linear"
								isAnimationActive={false}
								stroke={chartConfig[`season${seasonNum}`]?.color}
								strokeWidth={2}
								dot={true}
								connectNulls={false}
							/>
						))}
					</LineChart>
				</ChartContainer>
			</CardContent>
		</Card>
	)
}

const CustomTooltip = ({
	active,
	payload,
}: TooltipProps<string | number, string>) => {
	if (!active || !payload || payload.length == 0) {
		return null
	}
	const activeData = payload.find((item: any) => item.value !== null)
	if (!activeData) {
		return null
	}
	const seasonNum = activeData.dataKey?.toString()?.replace('season', '')
	const episode = activeData.payload[`episode${seasonNum}`] as Episode
	if (!episode) {
		return null
	}

	return (
		<Card>
			<CardHeader>
				<CardTitle>
					S{episode.seasonNum}E{episode.episodeNum}:
				</CardTitle>
				<CardDescription>{episode.title}</CardDescription>
			</CardHeader>
			<CardContent>
				<CardDescription>
					{episode.rating.toFixed(1)} / 10.0 (
					{episode.numVotes.toLocaleString()} votes)
				</CardDescription>
			</CardContent>
		</Card>
	)
}

function transformRatingsData(ratings: Ratings): {
	data: ChartDataPoint[]
	seasons: number[]
} {
	let episodeIndex = 1
	const data: ChartDataPoint[] = []
	const seasons: number[] = []

	for (const [seasonNumber, seasonRatings] of Object.entries(
		ratings.allEpisodeRatings,
	)) {
		const seasonNum = Number.parseInt(seasonNumber, 10)
		seasons.push(seasonNum)
		for (const episode of Object.values(seasonRatings)) {
			if (episode.numVotes === 0 || episode.episodeNum <= 0) {
				continue
			}
			const dataPoint: ChartDataPoint = {
				episodeIndex,
			}
			dataPoint[`season${seasonNum}`] = episode.rating
			dataPoint[`episode${seasonNum}`] = episode
			data.push(dataPoint)
			episodeIndex++
		}
	}
	return { data, seasons }
}
