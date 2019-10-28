package org.aria.imdbgraph.scrapper;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.aria.imdbgraph.scrapper.FileDownloader.ImdbFile;
import static org.aria.imdbgraph.scrapper.FileDownloader.ImdbFile.*;

/**
 * {@code DatabaseUpdater} is a utility class whose responsibility is to
 * download all the files provided by IMDB and load them into the database in
 * bulk. Since IMDB updates their files daily, this class should be scheduled to
 * run everyday.
 */
@Service
public class DatabaseUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUpdater.class);

    private final DataSource dataSource;
    private final FileDownloader fileDownloader;
    private BaseConnection connection;
    private Statement statement;

    /**
     * Constructor to initialize the {@code DatabaseUpdater} with all its
     * required dependencies
     *
     * @param dataSource     The {@code DataSource} object representing the
     *                       database that you want to be updated.
     * @param fileDownloader The {@code FileDownloader} class responsible for
     *                       downloading and preparing all the files which will
     *                       be read into the database.
     *                       <p>
     *                       Note: This dependency can be mocked for unit
     *                       testing so that you don't have to download a file
     *                       every time you test this class.
     */
    @Autowired
    public DatabaseUpdater(DataSource dataSource,
                           FileDownloader fileDownloader) {
        this.dataSource = dataSource;
        this.fileDownloader = fileDownloader;
    }

    /**
     * Method that will begin downloading the latest IMDB files and updating the
     * database with that data.
     */
    public void loadAllFiles() {
        try (BaseConnection conn = dataSource.getConnection().unwrap(BaseConnection.class);
             Statement s = conn.createStatement()) {
            this.connection = conn;
            this.statement = s;
            this.connection.setAutoCommit(false);

            copyFiles();
            loadTitles();
            loadEpisodes();
            loadRatings();

            this.connection.commit();
        } catch (SQLException e) {
            throw new FileLoadingError(e);
        }
    }

    /**
     * Method that's responsible for downloading all the files from IMDB and
     * loading them into temporary tables in the database. Once these files
     * are copied into the temp tables, they will soon be reformatted and loaded
     * into the real tables.
     */
    private void copyFiles() throws SQLException {
        statement.execute("" +
                "CREATE TEMPORARY TABLE temp_title\n" +
                "(\n" +
                "    imdb_id         VARCHAR(10) PRIMARY KEY,\n" +
                "    title_type      TEXT,\n" +
                "    primary_title   TEXT,\n" +
                "    original_title  TEXT,\n" +
                "    is_adult        BOOLEAN,\n" +
                "    start_year      CHAR(4),\n" +
                "    end_year        CHAR(4),\n" +
                "    runtime_minutes INT,\n" +
                "    genres          TEXT\n" +
                ") ON COMMIT DROP;\n" +
                "" +
                "CREATE TEMPORARY TABLE temp_episode\n" +
                "(\n" +
                "    episode_id  VARCHAR(10) PRIMARY KEY,\n" +
                "    show_id     VARCHAR(10),\n" +
                "    season_num  INT,\n" +
                "    episode_num INT\n" +
                ") ON COMMIT DROP;\n " +
                "" +
                "CREATE TEMPORARY TABLE temp_ratings\n" +
                "(\n" +
                "    imdb_id     VARCHAR(10),\n" +
                "    imdb_rating DOUBLE PRECISION,\n" +
                "    num_votes   INT\n" +
                ") ON COMMIT DROP;\n ");

        Map<ImdbFile, String> fileToTableName = Map.ofEntries(
                entry(TITLES_FILE, "temp_title"),
                entry(EPISODES_FILE, "temp_episode"),
                entry(RATINGS_FILE, "temp_ratings")
        );

        Set<ImdbFile> filesToDownload = fileToTableName.keySet();
        Map<ImdbFile, Path> filePaths = fileDownloader.download(filesToDownload);
        for (Map.Entry<ImdbFile, String> e : fileToTableName.entrySet()) {
            ImdbFile fileToLoad = e.getKey();
            String tableName = e.getValue();
            try (BufferedReader fileReader = Files.newBufferedReader(filePaths.get(fileToLoad))) {
                CopyManager copier = new CopyManager(connection);
                //language=SQL
                String command = "" +
                        "COPY %s\n" +
                        "FROM STDIN\n" +
                        "WITH (DELIMITER '\t');\n";
                copier.copyIn(String.format(command, tableName), fileReader);
                logger.info("{} successfully transferred to table {}", fileToLoad.getDownloadUrl(), tableName);
            } catch (SQLException | IOException exception) {
                throw new FileLoadingError(exception);
            }
        }
    }

    private void loadTitles() throws SQLException {
        statement.execute("" +
                "INSERT INTO imdb.rateable_title(imdb_id)\n" +
                "SELECT imdb_id\n" +
                "FROM temp_title\n " +
                "WHERE title_type = 'tvSeries' OR title_type = 'tvEpisode'\n" +
                "ON CONFLICT DO NOTHING;\n" +
                "\n" +
                "INSERT INTO imdb.show(imdb_id, primary_title, start_year, end_year)\n" +
                "SELECT imdb_id, primary_title, start_year, end_year\n" +
                "FROM temp_title\n" +
                "WHERE title_type = 'tvSeries'\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "SET primary_title = EXCLUDED.primary_title,\n" +
                "    start_year    = EXCLUDED.start_year,\n" +
                "    end_year      = EXCLUDED.end_year;\n" +
                "\n" +
                "INSERT INTO imdb.episode(episode_id, episode_title)\n" +
                "SELECT imdb_id, primary_title\n" +
                "FROM temp_title\n" +
                "WHERE title_type = 'tvEpisode'" +
                "ON CONFLICT (episode_id) DO UPDATE\n" +
                "SET episode_title = EXCLUDED.episode_title;");
        logger.info("Titles successfully loaded");
    }

    private void loadRatings() throws SQLException {
        statement.execute("" +
                "UPDATE imdb.rateable_title r\n" +
                "SET imdb_rating = temp.imdb_rating,\n" +
                "    num_votes = temp.num_votes\n" +
                "FROM temp_ratings temp\n" +
                "WHERE r.imdb_id = temp.imdb_id;\n");
        logger.info("Ratings successfully loaded");
    }

    private void loadEpisodes() throws SQLException {
        statement.execute("" +
                "INSERT INTO imdb.episode(show_id, episode_id, season_num, episode_num) " +
                "SELECT show_id, episode_id, season_num, episode_num " +
                "FROM temp_episode " +
                "WHERE show_id IN (SELECT imdb_id FROM imdb.show) " +
                "ON CONFLICT (episode_id) DO UPDATE " +
                "SET show_id       = EXCLUDED.show_id," +
                "    season_num    = EXCLUDED.season_num," +
                "    episode_num   = EXCLUDED.episode_num;");
        logger.info("Episodes successfully loaded");
    }

    private static class FileLoadingError extends RuntimeException {
        private FileLoadingError(Throwable cause) {
            super(cause);
        }
    }
}
