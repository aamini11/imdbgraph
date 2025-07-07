import { show, episode } from "@/db/schema";
import { Episode, Ratings } from "@/lib/data/types";
import { eq, asc } from "drizzle-orm";
import { NodePgDatabase } from "drizzle-orm/node-postgres";

export async function getRatings(
  db: NodePgDatabase,
  showId: string,
): Promise<Ratings> {
  const result = await db.select().from(show).where(eq(show.imdbId, showId));
  if (!result.length) {
    throw new Error("Show not found");
  }
  const foundShow = result[0];

  const episodes = await db
    .select({
      title: episode.title,
      seasonNum: episode.seasonNum,
      episodeNum: episode.episodeNum,
      numVotes: episode.numVotes,
      rating: episode.rating,
    })
    .from(episode)
    .where(eq(episode.showId, showId))
    .orderBy(asc(episode.seasonNum), asc(episode.episodeNum));

  // Group episodes by season and episode number (using string keys)
  const groupedEpisodes: Record<number, Record<number, Episode>> = {};
  for (const episodeInfo of episodes) {
    const { seasonNum, episodeNum } = episodeInfo;

    // Create season entry if missing
    groupedEpisodes[seasonNum] ??= {};
    // Add episode to season
    groupedEpisodes[seasonNum][episodeNum] = episodeInfo;
  }

  return { show: foundShow, allEpisodeRatings: groupedEpisodes };
}
