package org.aria.imdbgraph.scrapper;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

class FileUtil {

    private FileUtil() {}

   // private static final Path BASE_PATH = loadBasePath();

    static final String RATINGS_FILE_URL = "https://datasets.imdbws.com/title.ratings.tsv.gz";
    static final String EPISODES_FILE_URL = "https://datasets.imdbws.com/title.episode.tsv.gz";
    static final String SHOW_FILE_URL = "https://datasets.imdbws.com/name.basics.tsv.gz";

    static Resource openUrl(String url) {
        try {
            InputStream stream = new URL(url).openStream();
            InputStream unzipedStream = new GZIPInputStream(stream);
            return new InputStreamResource(unzipedStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

//    private static Path loadBasePath() {
//        URL url = FileUtil.class.getClassLoader().getResource("data");
//        if (url == null) {
//            throw new IllegalArgumentException("Data directory not found");
//        }
//
//        try {
//            return Paths.get(url.toURI());
//        } catch (URISyntaxException e) {
//            throw new IllegalArgumentException(e);
//        }
//    }
}
