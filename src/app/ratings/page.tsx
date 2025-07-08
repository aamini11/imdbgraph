import { Graph } from "@/components/graph";
import { getRatings } from "@/lib/data/ratings";
import { formatYears, Ratings } from "@/lib/data/types";
import { notFound } from "next/navigation";

export default async function RatingsPage({
  searchParams,
}: {
  searchParams: Promise<{ id?: string }>;
}) {
  const id = (await searchParams).id;
  if (!id) {
    notFound();
  }

  const ratings = await getRatings(id);
  const show = ratings.show;

  return (
    <>
      {/* Show Title */}
      <div className="p-3">
        <h1 className="text-center text-xl">
          {show.title} ({formatYears(show)})
        </h1>
        <h2 className="text-center text-sm">
          Show rating: {show.rating.toFixed(1)} (Votes:{" "}
          {show.numVotes.toLocaleString()})
        </h2>
      </div>

      {/* Graph */}
      <div className="min-h-[250px] flex-1 p-5">
        {!hasRatings(ratings) ? (
          <h1 className="pt-8 text-center text-6xl leading-tight">
            No Ratings Found
          </h1>
        ) : (
          <Graph ratings={ratings} />
        )}
      </div>
    </>
  );
}

function hasRatings(ratings: Ratings): boolean {
  for (const seasonRatings of Object.values(ratings.allEpisodeRatings)) {
    for (const episode of Object.values(seasonRatings)) {
      if (episode.numVotes > 0) {
        return true;
      }
    }
  }
  return false;
}
