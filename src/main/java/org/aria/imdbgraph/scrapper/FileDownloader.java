package org.aria.imdbgraph.scrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Utility class whose responsibility is to download and formats files provided
 * by IMDB which contain all the data needed for this application. This includes
 * information about every TV show, each of its episodes, and all their ratings.
 * <p>
 * Documentation for file formats is available by IMDB themselves:
 * https://www.imdb.com/interfaces/
 */
@Service
class FileDownloader {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloader.class);
    private static final String BASE_DOWNLOAD_URL = "https://datasets.imdbws.com";

    private final Path baseDir;

    FileDownloader(@Value("${imdbgraph.data.directory}") String baseDir) {
        this.baseDir = Paths.get(baseDir);
    }

    /**
     * Enum to represent all the flat files provided by IMDB that need to be
     * parsed and downloaded into the database.
     */
    enum ImdbFile {
        TITLES_FILE("title.basics.tsv.gz"),
        EPISODES_FILE("title.episode.tsv.gz"),
        RATINGS_FILE("title.ratings.tsv.gz");

        private final String fileName;

        ImdbFile(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Helper method to remove the .gz file extension from a file name.
         */
        String getUnzippedFileName() {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }

        /**
         * Fetches the URL where the file can be downloaded.
         */
        URL getDownloadUrl() {
            String downloadURL = UriComponentsBuilder
                    .fromUriString(BASE_DOWNLOAD_URL)
                    .pathSegment(fileName)
                    .toUriString();
            try {
                return new URL(downloadURL);
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Downloads all the files from IMDB which contain the data needed to be
     * parsed and loaded into the database.
     * <p>
     * NOTE: This method also unzips the file.
     *
     * @param filesToDownload The download method accepts a set of
     *                        {@code ImdbFile} which represents all the IMDB
     *                        files you want this method to download.
     * @return If the method was able to download the files successfully, it
     * returns a map where each {@code ImdbFile} points to the path where that
     * file was downloaded.
     */
    Map<ImdbFile, Path> download(Set<ImdbFile> filesToDownload) {
        Map<ImdbFile, Path> downloadedFiles = new EnumMap<>(ImdbFile.class);
        for (ImdbFile f : filesToDownload) {
            URL downloadURL = f.getDownloadUrl();
            File outputFile = baseDir.resolve(f.getUnzippedFileName()).toFile();
            // unzips files as well.
            try (InputStream unzippedStream = new GZIPInputStream(downloadURL.openStream());
                 ReadableByteChannel rbc = Channels.newChannel(unzippedStream);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                if (outputFile.getParentFile().mkdirs()) {
                    logger.info("Creating path: {}", outputFile.getParentFile());
                }
                if (outputFile.createNewFile()) {
                    logger.info("Creating new file: {}", outputFile.getName());
                }

                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                logger.info("Downloaded file: {} to {}", downloadURL, outputFile);
                downloadedFiles.put(f, outputFile.toPath());
            } catch (IOException e) {
                throw new DatabaseUpdater.FileLoadingError(e);
            }
        }

        return downloadedFiles;
    }
}
