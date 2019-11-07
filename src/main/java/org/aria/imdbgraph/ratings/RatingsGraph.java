package org.aria.imdbgraph.ratings;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data class containing all the ratings data for a specific show.
 */
public final class RatingsGraph {

    private final Show show;
    private final Map<Integer, Map<Integer, Episode>> allEpisodeRatings;

    /**
     * Constructor to initialize {@link RatingsGraph} object.
     *
     * @param show              Show object which holds basic information about
     *                          the show along with its rating.
     * @param allEpisodeRatings A list containing every episode rating for a
     *                          show.
     */
    RatingsGraph(Show show, List<Episode> allEpisodeRatings) {
        this.show = show;
        this.allEpisodeRatings = toMap(allEpisodeRatings);
    }

    /**
     * Returns a #{@link Show} object which contains information about the
     * show for each {@link RatingsGraph} object.
     *
     * @return #{@link Show} The show object
     */
    public Show getShow() {
        return show;
    }

    /**
     * Returns ratings information for a show as a 2D map where
     * (season number, episode number) -> episode rating.
     *
     * @return The episode to rating map.
     */
    public Map<Integer, Map<Integer, Episode>> getAllEpisodeRatings() {
        return allEpisodeRatings;
    }

    /**
     * Returns ratings information for a specific episode of a show
     *
     * @param season  The season number
     * @param episode The episode number
     * @return {@link Episode} object containing ratings information
     */
    public Episode getEpisode(int season, int episode) {
        if (allEpisodeRatings.containsKey(season)) {
            Map<Integer, Episode> seasonRatings = allEpisodeRatings.get(season);
            if (seasonRatings.containsKey(episode)) {
                return seasonRatings.get(episode);
            }
        }

        String errMsg = String.format("Episode: %d Season: %d does not exist", season, episode);
        throw new InvalidParameterException(errMsg);
    }

    private static Map<Integer, Map<Integer, Episode>> toMap(List<Episode> episodeSequence) {
        Map<Integer, Map<Integer, Episode>> map = new LinkedHashMap<>();
        for (Episode e : episodeSequence) {
            Map<Integer, Episode> seasonRatings = map.computeIfAbsent(e.getSeason(), key -> new LinkedHashMap<>());
            seasonRatings.put(e.getEpisodeNumber(), e);
        }
        return map;
    }
}
