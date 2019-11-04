package org.aria.imdbgraph.scrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static java.nio.file.StandardOpenOption.*;

/**
 * Utility class whose responsibility is to download and formats files provided
 * by IMDB which contain all the data needed for this application. This includes
 * information about every TV show, each of its episodes, and all their ratings.
 * <p>
 * Documentation for file formats is available by IMDB themselves:
 * https://www.imdb.com/interfaces/
 */
@Service
public class FileDownloader {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloader.class);
    private static final String BASE_DOWNLOAD_URL = "https://datasets.imdbws.com";

    private final Path baseDir;

    public FileDownloader(@Value("${imdbgraph.data.directory}") String baseDir) {
        this.baseDir = Paths.get(baseDir);
    }

    /**
     * Enum to represent all the flat files provided by IMDB that need to be
     * parsed and downloaded into the database.
     */
    public enum ImdbFile {
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
     * NOTE: This method also unzips the file and removes its first line
     * (header).
     *
     * @param filesToDownload The download method accepts a set of
     *                        {@code ImdbFile} which represents all the IMDB
     *                        files you want this method to download.
     * @return If the method was able to download the files successfully, it
     * returns a map where each {@code ImdbFile} points to the path where that
     * file was downloaded.
     */
    public Map<ImdbFile, Path> download(Set<ImdbFile> filesToDownload) {
        Map<ImdbFile, Path> downloadedFiles = new EnumMap<>(ImdbFile.class);
        for (ImdbFile f : filesToDownload) {
            Path downloadLocation = baseDir.resolve(f.getUnzippedFileName());
            URL downloadURL = f.getDownloadUrl();
            try (InputStream downloadStream = new GZIPInputStream(downloadURL.openStream()); // unzips files as well.
                 BufferedReader reader = new BufferedReader(new InputStreamReader(downloadStream));
                 BufferedWriter writer = Files.newBufferedWriter(downloadLocation, TRUNCATE_EXISTING)) {
                File outputFile = downloadLocation.toFile();
                if (outputFile.getParentFile().mkdirs()) {
                    logger.info("Creating path: {}", outputFile.getParentFile());
                }
                if (outputFile.createNewFile()) {
                    logger.info("Creating new file: {}", outputFile.getName());
                }

                String line;
                for (int i = 0; (line = reader.readLine()) != null; i++) {
                    // Skip first line since it's a header and messes with
                    // posgresql's COPY command.
                    if (i == 0) continue;
                    writer.write(line);
                    writer.newLine();
                }
                logger.info("Downloaded file: {}", downloadURL);
                downloadedFiles.put(f, downloadLocation);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return downloadedFiles;
    }
}
