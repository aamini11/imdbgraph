package org.aria.imdbgraph.imdb;

import java.util.*;

/**
 * Data class containing all the ratings of a show and basic information about that show.
 */
public final class Ratings {

    private final Show show;
    private final SortedMap<Integer, SortedMap<Integer, Episode>> allRatings;

    private static SortedMap<Integer, SortedMap<Integer, Episode>> toMap(List<Episode> episodeSequence) {
        SortedMap<Integer, SortedMap<Integer, Episode>> map = new TreeMap<>();
        for (Episode e : episodeSequence) {
            SortedMap<Integer, Episode> seasonRatings = map.computeIfAbsent(e.getSeason(), (key) -> new TreeMap<>());
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

    public SortedMap<Integer, SortedMap<Integer, Episode>> getAllRatings() {
        return allRatings;
    }
}
