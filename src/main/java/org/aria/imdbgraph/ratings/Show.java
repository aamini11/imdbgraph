package org.aria.imdbgraph.ratings;

/**
 * Immutable data class to represent information about a show. Includes rating
 * information about the show (avg rating, total votes).
 */
public final class Show {

    private final String imdbId;
    private final String title;
    private final String startYear;
    private final String endYear;
    private final double showRating;
    private final int numVotes;

    /**
     * Constructor used to initialize show information.
     *
     * @param imdbId The IMDB id of the show.
     * @param title Title of the show
     * @param startYear Year show began
     * @param endYear Year show ended
     * @param showRating Average rating of a show (0.0-10.0)
     * @param numVotes Number of people that voted for that episode (> 0)
     */
    Show(String imdbId, String title, String startYear, String endYear, double showRating, int numVotes) {
        this.imdbId = imdbId;
        this.title = title;
        this.startYear = startYear;
        this.endYear = endYear;
        this.showRating = showRating;
        this.numVotes = numVotes;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getTitle() {
        return title;
    }

    public String getStartYear() {
        return startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public double getShowRating() {
        return showRating;
    }

    public int getNumVotes() {
        return numVotes;
    }
}
