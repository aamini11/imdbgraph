package org.aria.imdbgraph.scrapper;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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

        final String fileName;
        final URL downloadUrl;

        ImdbFlatFile(String fileName) {
            try {
                this.fileName = fileName;
                this.downloadUrl = new URL(BASE_URL + "/" + fileName);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }

        private static final String BASE_URL = "https://datasets.imdbws.com";

        String getOutputFileName() {
            return fileName.substring(0, fileName.lastIndexOf('.')); //remove .gz extension.
        }
    }

    /**
     * Convert IMDB flat file to a Spring resource to be used by the Item reader.
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
            try (InputStream in = new GZIPInputStream(file.downloadUrl.openStream())) { // unzips files as well.
                Files.copy(in, downloadLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
