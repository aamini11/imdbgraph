export interface Show {
  imdbId: string;
  title: string;
  startYear: string;
  endYear: string | null;
  rating: number;
  numVotes: number;
}

export interface Episode {
  title: string;
  seasonNum: number;
  episodeNum: number;
  rating: number;
  numVotes: number;
}

export interface Ratings {
  show: Show;
  allEpisodeRatings: Record<string, Record<string, Episode>>;
}

export function formatYears(show: Show): string {
  const endDate = show.endYear ?? "Present";
  return `${show.startYear} - ${endDate}`;
}
