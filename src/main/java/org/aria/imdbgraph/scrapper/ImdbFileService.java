package org.aria.imdbgraph.scrapper;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
 * Service class that provides basic operations involving the flat files IMDB provides for show/ratings data
 * <p>
 * Documentation for each file: https://www.imdb.com/interfaces/
 */
class ImdbFileService {

    private final Path baseDir;

    /**
     * Package private constructor to build the file service class.
     * @param baseDataDirectory The base directory where the IMDB files will be downloaded
     */
    ImdbFileService(String baseDataDirectory) {
        baseDir = Paths.get(baseDataDirectory);
    }

    /**
     * Enum to represent all the flat files provided by IMDB that need to be parsed and downloaded into the database.
     */
    enum ImdbFlatFile {

        TITLES_FILE("title.basics.tsv.gz"),
        EPISODES_FILE("title.episode.tsv.gz"),
        RATINGS_FILE("title.ratings.tsv.gz");

        private static final String BASE_URL = "https://datasets.imdbws.com";

        final String inputFileName;
        final URL downloadUrl;

        /**
         * Constructors an IMDB file enum using only the name of the file to be downloaded from
         * this website: https://datasets.imdbws.com
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
         * @return file name after its extension has been removed.
         */
        String unzippedFileName() {
            return inputFileName.substring(0, inputFileName.lastIndexOf('.'));
        }
    }


    /**
     * Convert IMDB flat file to a Spring resource. Mainly used as helper method when setting up the file item readers.
     *
     * @param file The Imdb flat file to be converted
     * @return The flat file now represented as a spring resource.
     */
    Resource toResource(ImdbFlatFile file) {
        return new FileSystemResource(baseDir.resolve(file.unzippedFileName()));
    }

    /**
     * Downloads all IMDB flat files and stores them in the data directory.
     */
    void downloadAllFiles() {
        Set<ImdbFlatFile> allFiles = EnumSet.allOf(ImdbFlatFile.class);
        for (ImdbFlatFile file : allFiles) {
            Path downloadLocation = baseDir.resolve(file.unzippedFileName());
            try (InputStream in = new GZIPInputStream(file.downloadUrl.openStream())) { // unzips files as well.
                Files.copy(in, downloadLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
