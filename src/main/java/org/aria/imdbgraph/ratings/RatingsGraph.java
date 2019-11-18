package org.aria.imdbgraph.ratings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable data class containing all the ratings information about a specific
 * show, along with information about the TV show itself.
 *
 * Note: This class will also be serialized as a JSON object and returned to the
 * front-end where it will be rendered as an actual graph.
 */
public final class RatingsGraph {

    private final Show show;
    private final Map<Integer, Map<Integer, Episode>> allEpisodeRatings;

    RatingsGraph(Show show, List<Episode> allEpisodeRatings) {
        this.show = show;
        this.allEpisodeRatings = toMap(allEpisodeRatings);
    }

    /**
     * Getter method which returns which show this {@code RatingsObject} object
     * is supposed to hold episode ratings for.
     *
     * @return A {@link Show} object containing all meta-data and ratings
     * information about the show.
     */
    @JsonProperty("show")
    public Show getShow() {
        return show;
    }

    /**
     * Getter method that returns all episode ratings data as a map.
     *
     * @return 2D map where (season number, episode number) points to (episode rating).
     */
    @JsonProperty("episodeRatings")
    public Map<Integer, Map<Integer, Episode>> getAllEpisodeRatings() {
        return allEpisodeRatings;
    }

    /**
     * Utility method that retuns ratings information for a specific episode
     * of a show
     *
     * @param season  The season number
     * @param episode The episode number
     * @return {@link Episode} object containing the ratings data.
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
