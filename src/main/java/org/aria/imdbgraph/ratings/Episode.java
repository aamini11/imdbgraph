package org.aria.imdbgraph.ratings;

/**
 * Immutable data class containing all the meta-data and ratings information
 * about a specific TV episode.
 *
 * Note: This class will also be serialized as a JSON object
 */
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

    /**
     * Title of the episode
     */
    public String getEpisodeTitle() {
        return episodeTitle;
    }

    /**
     * Season number of episode
     */
    public int getSeason() {
        return season;
    }

    /**
     * Returns episode number
     */
    public int getEpisodeNumber() {
        return episodeNumber;
    }

    /**
     * The average IMDB rating of the episode (0.0 - 10.0)
     */
    public double getImdbRating() {
        return imdbRating;
    }

    /**
     * Total number of votes for the episode (&gt; 0)
     */
    public int getNumVotes() {
        return numVotes;
    }
}
