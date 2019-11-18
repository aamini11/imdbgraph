package org.aria.imdbgraph.ratings;

/**
 * Immutable data class to represent all meta-data and ratings information
 * about a specific TV show.
 *
 * Note: This class will also be serialized as a JSON object.
 */
public final class Show {

    private final String imdbId;
    private final String title;
    private final String startYear;
    private final String endYear;
    private final double showRating;
    private final int numVotes;

    Show(String imdbId, String title, String startYear, String endYear, double showRating, int numVotes) {
        this.imdbId = imdbId;
        this.title = title;
        this.startYear = startYear;
        this.endYear = endYear;
        this.showRating = showRating;
        this.numVotes = numVotes;
    }

    /**
     * @return A unique ID which IMDB assigns to every TV show, Movie, Episode, etc...
     */
    public String getImdbId() {
        return imdbId;
    }

    /**
     * @return The title of the TV show.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The year the TV show started.
     */
    public String getStartYear() {
        return startYear;
    }

    /**
     * @return The year the TV show ended (Can be null if it hasn't ended yet)
     */
    public String getEndYear() {
        return endYear;
    }

    /**
     * @return The average rating of the TV show from a scale of (0.0 - 10.0)
     */
    public double getShowRating() {
        return showRating;
    }

    /**
     * @return The number of people that voted on the show's rating. Can be 0
     * if nobody has voted on the TV show.
     */
    public int getNumVotes() {
        return numVotes;
    }
}
