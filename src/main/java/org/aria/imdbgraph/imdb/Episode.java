package org.aria.imdbgraph.imdb;

public final class Episode {

    private final String episodeTitle;
    private final int season;
    private final int episodeNumber;
    private final double imdbRating;
    private final int numVotes;

    Episode(String episodeTitle, int season, int episodeNumber, double imdbRating, int numVotes) {
        this.episodeTitle = episodeTitle;
        this.season = season;
        this.episodeNumber = episodeNumber;
        this.imdbRating = imdbRating;
        this.numVotes = numVotes;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public int getSeason() {
        return season;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    public int getNumVotes() {
        return numVotes;
    }
}
