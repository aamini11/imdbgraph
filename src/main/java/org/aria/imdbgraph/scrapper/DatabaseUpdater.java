package org.aria.imdbgraph.scrapper;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
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
    private final JdbcTemplate jdbcTemplate;

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
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Method that will begin downloading the latest IMDB files and updating the
     * database with that data.
     */
    @Transactional
    public void loadAllFiles() {
        try {
            copyFiles();
            loadShows();
            loadEpisodes();
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
        jdbcTemplate.execute("" +
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
                ") ON COMMIT DROP;\n " +

                "CREATE TEMPORARY TABLE temp_episode\n" +
                "(\n" +
                "    episode_id  VARCHAR(10) PRIMARY KEY,\n" +
                "    show_id     VARCHAR(10),\n" +
                "    season_num  INT,\n" +
                "    episode_num INT\n" +
                ") ON COMMIT DROP;\n" +

                "CREATE TEMPORARY TABLE temp_ratings\n" +
                "(\n" +
                "    imdb_id     VARCHAR(10),\n" +
                "    imdb_rating DOUBLE PRECISION,\n" +
                "    num_votes   INT\n" +
                ") ON COMMIT DROP;");

        Map<ImdbFile, String> fileToTableName = Map.ofEntries(
                entry(TITLES_FILE, "temp_title"),
                entry(EPISODES_FILE, "temp_episode"),
                entry(RATINGS_FILE, "temp_ratings")
        );

        BaseConnection postgresConnection = DataSourceUtils.getConnection(dataSource)
                .unwrap(BaseConnection.class);

        Set<ImdbFile> filesToDownload = fileToTableName.keySet();
        Map<ImdbFile, Path> filePaths = fileDownloader.download(filesToDownload);
        for (Map.Entry<ImdbFile, String> e : fileToTableName.entrySet()) {
            ImdbFile fileToLoad = e.getKey();
            String tableName = e.getValue();
            Path pathOfFileToLoad = filePaths.get(fileToLoad);
            try (Reader fileReader = Files.newBufferedReader(pathOfFileToLoad)) {
                CopyManager copier = new CopyManager(postgresConnection);
                //language=SQL
                String command = "" +
                        "COPY %s\n" +
                        "FROM STDIN\n" +
                        "WITH (DELIMITER '\t');";
                copier.copyIn(String.format(command, tableName), fileReader);

                logger.info("{} successfully transferred to table {}",
                        fileToLoad.getDownloadUrl(),
                        tableName);
            } catch (SQLException | IOException exception) {
                throw new FileLoadingError(exception);
            }
        }
    }

    private void loadShows() {
        jdbcTemplate.execute("" +
                "INSERT INTO imdb.show(imdb_id,\n" +
                "                      primary_title,\n" +
                "                      start_year,\n" +
                "                      end_year,\n" +
                "                      imdb_rating,\n" +
                "                      num_votes)\n" +
                "SELECT imdb_id,\n" +
                "       primary_title,\n" +
                "       start_year,\n" +
                "       end_year,\n" +
                "       COALESCE(imdb_rating, 0.0), \n" +
                "       COALESCE(num_votes, 0)\n" +
                "FROM temp_title\n" +
                "         LEFT JOIN temp_ratings USING (imdb_id)\n" +
                "WHERE title_type = 'tvSeries'\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "    SET primary_title = excluded.primary_title,\n" +
                "        start_year    = excluded.start_year,\n" +
                "        end_year      = excluded.end_year,\n" +
                "        imdb_rating   = excluded.imdb_rating,\n" +
                "        num_votes     = excluded.num_votes;");
        logger.info("Shows successfully updated");
    }

    private void loadEpisodes() {
        jdbcTemplate.execute("" +
                "INSERT INTO imdb.episode(show_id,\n" +
                "                         episode_id,\n" +
                "                         episode_title,\n" +
                "                         season_num,\n" +
                "                         episode_num,\n" +
                "                         imdb_rating,\n" +
                "                         num_votes)\n" +
                "SELECT show_id,\n" +
                "       episode_id,\n" +
                "       primary_title,\n" +
                "       season_num,\n" +
                "       episode_num,\n" +
                "       COALESCE(imdb_rating, 0.0),\n" +
                "       COALESCE(num_votes, 0)\n" +
                "FROM temp_episode\n" +
                "         JOIN temp_title ON (episode_id = imdb_id)\n" +
                "         LEFT JOIN temp_ratings USING (imdb_id)\n" +
                "WHERE show_id IN (SELECT imdb_id FROM imdb.show)\n" +
                "  AND season_num > 0\n" +
                "  AND episode_num > 0\n" +
                "ON CONFLICT (episode_id) DO UPDATE\n" +
                "    SET show_id       = excluded.show_id,\n" +
                "        episode_title = excluded.episode_title,\n" +
                "        season_num    = excluded.season_num,\n" +
                "        episode_num   = excluded.episode_num,\n" +
                "        imdb_rating   = excluded.imdb_rating,\n" +
                "        num_votes     = excluded.num_votes;");
        logger.info("Episodes successfully updated");
    }

    private static class FileLoadingError extends RuntimeException {
        private FileLoadingError(Throwable cause) {
            super(cause);
        }
    }
}
