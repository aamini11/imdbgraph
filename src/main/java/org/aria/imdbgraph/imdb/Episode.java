package org.aria.imdbgraph.imdb;

public final class Episode {

    private final String episodeTitle;
    private final int season;
    private final int episode;
    private final double imdbRating;
    private final int numVotes;

    Episode(String episodeTitle, int season, int episode, double imdbRating, int numVotes) {
        this.episodeTitle = episodeTitle;
        this.season = season;
        this.episode = episode;
        this.imdbRating = imdbRating;
        this.numVotes = numVotes;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public int getSeason() {
        return season;
    }

    public int getEpisode() {
        return episode;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    public int getNumVotes() {
        return numVotes;
    }
}
