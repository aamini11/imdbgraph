package org.aria.imdbgraph.imdb;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Data class to hold information about a specific episode of a show include the
 * IMDB ratings for that episode.
 */
public final class Episode {

    private final String episodeTitle;
    private final int season;
    private final int episodeNumber;
    private final double imdbRating;
    private final int numVotes;

    /**
     * Constructor used to initialize episode information
     * @param episodeTitle Title of the episode
     * @param season Season number of episode
     * @param episodeNumber Episode number of episode
     * @param imdbRating The average IMDB rating of the episode (0.0 - 10.0)
     * @param numVotes Total number of votes for the episode (> 0)
     */
    @JsonCreator
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
