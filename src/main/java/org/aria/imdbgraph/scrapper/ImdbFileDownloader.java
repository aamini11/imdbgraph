package org.aria.imdbgraph.scrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Service class that provides basic operations involving the flat files IMDB
 * provides for show/ratings data
 * <p>
 * Documentation for each file: https://www.imdb.com/interfaces/
 */
@Service
class ImdbFileDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ImdbFileDownloader.class);

    private final Path baseDir;

    /**
     * Package private constructor to build the file service class.
     *
     * @param downloadLocation The directory location where the IMDB files
     *                         will be downloaded
     */
    ImdbFileDownloader(@Value("${imdbgraph.data.directory}") String downloadLocation) {
        baseDir = Paths.get(downloadLocation);
    }

    /**
     * Enum to represent all the flat files provided by IMDB that need to be
     * parsed and downloaded into the database.
     */
    enum ImdbFlatFile {

        TITLES_FILE("title.basics.tsv.gz"),
        EPISODES_FILE("title.episode.tsv.gz"),
        RATINGS_FILE("title.ratings.tsv.gz");

        private static final String BASE_URL = "https://datasets.imdbws.com";

        final String inputFileName;
        final URL downloadUrl;

        /**
         * Constructors an IMDB file enum using only the name of the file to be
         * downloaded from
         * this website: https://datasets.imdbws.com
         *
         * @param inputFileName The file name
         */
        ImdbFlatFile(String inputFileName) {
            this.inputFileName = inputFileName;

            String downloadURL = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .pathSegment(inputFileName)
                    .toUriString();
            try {
                this.downloadUrl = new URL(downloadURL);
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Helper method to remove the .gz file extension from a file name.
         *
         * @return file name after its extension has been removed.
         */
        String unzippedFileName() {
            return inputFileName.substring(0, inputFileName.lastIndexOf('.'));
        }
    }

    /**
     * Gets the path where each imdb file will be downloaded
     */
    Path getPath(ImdbFlatFile file) {
        return baseDir.resolve(file.unzippedFileName());
    }

    /**
     * Downloads all IMDB flat files and stores them in the data directory.
     */
    void downloadAllFiles() {
        Set<ImdbFlatFile> allFiles = EnumSet.allOf(ImdbFlatFile.class);
        for (ImdbFlatFile file : allFiles) {
            Path downloadLocation = baseDir.resolve(file.unzippedFileName());
            // unzips files as well.
            try (InputStream in = new GZIPInputStream(file.downloadUrl.openStream())) {
                File outputFile = downloadLocation.toFile();
                if (outputFile.getParentFile().mkdirs()) {
                    logger.info("Creating path: {}", outputFile.getParentFile());
                }
                if (outputFile.createNewFile()) {
                    logger.info("Creating new file: {}", outputFile.getName());
                }
                Files.copy(in, downloadLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
