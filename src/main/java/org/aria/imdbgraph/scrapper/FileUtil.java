package org.aria.imdbgraph.scrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;

class FileUtil {

    private FileUtil() {
    }

    private static final Path basePath = loadBasePath();

    static final String TITLES_FILE_URL = "https://datasets.imdbws.com/title.basics.tsv.gz";
    static final String EPISODES_FILE_URL = "https://datasets.imdbws.com/title.episode.tsv.gz";
    static final String RATINGS_FILE_URL = "https://datasets.imdbws.com/title.ratings.tsv.gz";

    static final Path TITLES_FILE_PATH = basePath.resolve("title.basics.tsv.txt");
    static final Path EPISODES_FILE_PATH = basePath.resolve("title.episode.tsv.txt");
    static final Path RATINGS_FILE_PATH = basePath.resolve("title.ratings.tsv.txt");

    /**
     * Loads the data directory where all the IMDB flat files will be stored
     *
     * @return The Path to the data directory where all IMDB flat files will be stored. (classpath:data/...)
     */
    private static Path loadBasePath() {
        URL url = FileUtil.class.getClassLoader().getResource("data");
        if (url == null) {
            throw new IllegalArgumentException("Data directory not found");
        }

        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Downloads and unzips a file
     *
     * @param fileUrl    URL to download file from.
     * @param outputPath Path where file will be saved.
     */
    static void downloadAndUnzipFile(URL fileUrl, Path outputPath) {
        try (InputStream in = new GZIPInputStream(fileUrl.openStream())) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
