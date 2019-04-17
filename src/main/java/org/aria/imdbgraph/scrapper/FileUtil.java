package org.aria.imdbgraph.scrapper;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

class FileUtil {

    private FileUtil() {
    }

    static final String RATING_FILE_NAME = "title.ratings.tsv";
    static final String EPISODES_FILE_NAME = "title.episode.tsv";
    static final String SHOW_FILE_NAME = "title.basics.tsv";

    static final String RATINGS_FILE_URL = "https://datasets.imdbws.com/title.ratings.tsv.gz";
    static final String EPISODES_FILE_URL = "https://datasets.imdbws.com/title.episode.tsv.gz";
    static final String SHOW_FILE_URL = "https://datasets.imdbws.com/title.basics.tsv.gz";

    static Resource openUrl(String url) {
        try {
            InputStream stream = new URL(url).openStream();
            InputStream unzippedStream = new GZIPInputStream(stream);
            return new InputStreamResource(unzippedStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
