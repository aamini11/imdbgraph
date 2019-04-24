package org.aria.imdbgraph.scrapper;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
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

class FileService {

    private final Path baseDir;

    FileService(String baseDataDirectory) {
        baseDir = Paths.get(baseDataDirectory);
    }

    /**
     * Enum to represent all flat files provided by IMDB that need to be downloaded and parsed.
     */
    enum ImdbFlatFile {

        TITLES_FILE("title.basics.tsv.gz"),
        EPISODES_FILE("title.episode.tsv.gz"),
        RATINGS_FILE("title.ratings.tsv.gz");

        private static final String BASE_URL = "https://datasets.imdbws.com";

        private final String fileName;
        private final URL downloadUrl;

        ImdbFlatFile(String fileName) {
            try {
                this.fileName = fileName;
                this.downloadUrl = new URL(BASE_URL + "/" + fileName);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }

        public String getInputFileName() {
            return fileName;
        }

        public String getOutputFileName() {
            return fileName.substring(0, fileName.lastIndexOf('.')); //remove .gz extension.
        }

        public URL getDownloadUrl() {
            return downloadUrl;
        }
    }


    /**
     * Convert IMDB flat file to a Spring resource. Mainly used as helper method to set up item readers.
     * @param file The file to convert to a resource
     * @return An IMDB flat file now wrapped in a spring resource.
     */
    Resource toResource(ImdbFlatFile file) {
        return new FileSystemResource(baseDir.resolve(file.getOutputFileName()));
    }

    /**
     * Downloads all IMDB flat files and stores them in the data directory.
     */
    void downloadAllFiles() {
        Set<ImdbFlatFile> allFiles = EnumSet.allOf(ImdbFlatFile.class);
        for (ImdbFlatFile file : allFiles) {
            Path downloadLocation = baseDir.resolve(file.getOutputFileName());
            try (InputStream in = new GZIPInputStream(file.getDownloadUrl().openStream())) { // unzips files as well.
                File outputFile = downloadLocation.toFile();
                if (!outputFile.exists()) {
                    boolean fileCreated = outputFile.createNewFile();
                    if (!fileCreated) throw new IOException("Unable to create file: " + outputFile.toString());
                }
                Files.copy(in, downloadLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}