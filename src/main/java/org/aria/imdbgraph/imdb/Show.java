package org.aria.imdbgraph.imdb;

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
