package org.aria.imdbgraph.imdb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data class containing all the ratings of a show and basic information about that show.
 */
public final class Ratings {

    private final Show show;
    private final Map<Integer, Map<Integer, Episode>> allRatings;

    private static Map<Integer, Map<Integer, Episode>> toMap(List<Episode> episodeSequence) {
        Map<Integer, Map<Integer, Episode>> map = new LinkedHashMap<>();
        for (Episode e : episodeSequence) {
            Map<Integer, Episode> seasonRatings = map.computeIfAbsent(e.getSeason(), (key) -> new LinkedHashMap<>());
            seasonRatings.put(e.getEpisode(), e);
        }
        return map;
    }

    Ratings(Show show, List<Episode> allRatings) {
        this.show = show;
        this.allRatings = toMap(allRatings);
    }

    public Show getShow() {
        return show;
    }

    public Map<Integer, Map<Integer, Episode>> getAllRatings() {
        return allRatings;
    }
}
