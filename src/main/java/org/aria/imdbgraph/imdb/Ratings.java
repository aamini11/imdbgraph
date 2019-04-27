package org.aria.imdbgraph.imdb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data class containing the rating data of a show and all its episodes.
 */
public final class Ratings {

    private final Show show;
    private final Map<Integer, Map<Integer, Episode>> allRatings;

    /**
     * @param show Show object which holds basic information about the show along with its rating.
     * @param allRatings A list containing every episode rating rating for a show.
     */
    Ratings(Show show, List<Episode> allRatings) {
        this.show = show;
        this.allRatings = toMap(allRatings);
    }

    public Show getShow() {
        return show;
    }

    /**
     * @return Returns the ratings of every episode as a 2D map where (season number, episode number) -> Episode.
     */
    public Map<Integer, Map<Integer, Episode>> getAllRatings() {
        return allRatings;
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
