package org.aria.imdbgraph.scrapper;

class FileUtil {

    private FileUtil() {
    }

    static final String RATING_FILE_NAME = "title.ratings.tsv";
    static final String EPISODES_FILE_NAME = "title.episode.tsv";
    static final String SHOW_FILE_NAME = "title.basics.tsv";

    static final String RATINGS_FILE_URL = "https://datasets.imdbws.com/title.ratings.tsv.gz";
    static final String EPISODES_FILE_URL = "https://datasets.imdbws.com/title.episode.tsv.gz";
    static final String SHOW_FILE_URL = "https://datasets.imdbws.com/title.basics.tsv.gz";

}
