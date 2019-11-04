package org.aria.imdbgraph;


import org.aria.imdbgraph.scrapper.DatabaseUpdater;
import org.aria.imdbgraph.scrapper.FileDownloader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import static org.aria.imdbgraph.scrapper.FileDownloader.ImdbFile;
import static org.aria.imdbgraph.scrapper.FileDownloader.ImdbFile.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class DatabaseLoaderUnitTests {

    private static final Path TEST_DIR = Paths.get("C:\\Users\\Aria\\IdeaProjects\\imdbgraph\\src\\test\\resources\\data");

    private static final String GAME_OF_THRONE_ID = "tt0944947";
    private static final String AVATAR_ID = "tt0417299";

    @Autowired
    private JdbcTemplate jdbc;

    @MockBean
    private FileDownloader fileDownloader;

    @Autowired
    private DatabaseUpdater databaseUpdater;

    private void clearTables() {
        jdbc.execute("DELETE FROM imdb.show");
        jdbc.execute("DELETE FROM imdb.episode");
    }

    @Test
    public void testSampleFiles() {
        clearTables();

        Map<ImdbFile, Path> filePaths = new EnumMap<>(ImdbFile.class);
        filePaths.put(TITLES_FILE, TEST_DIR.resolve("title_sample.tsv"));
        filePaths.put(RATINGS_FILE, TEST_DIR.resolve("ratings_sample.tsv"));
        filePaths.put(EPISODES_FILE, TEST_DIR.resolve("episode_sample.tsv"));
        when(fileDownloader.download(any())).thenReturn(filePaths);

        databaseUpdater.loadAllFiles();
        int epCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.episode");
        int showCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.show");
        Assert.assertEquals(3, showCount);
        Assert.assertEquals(139, epCount);
    }

    @Test
    public void testRealFiles() {
        clearTables();

        Map<ImdbFile, Path> filePaths = new EnumMap<>(ImdbFile.class);
        filePaths.put(TITLES_FILE, TEST_DIR.resolve("title.basics.tsv"));
        filePaths.put(RATINGS_FILE, TEST_DIR.resolve("title.ratings.tsv"));
        filePaths.put(EPISODES_FILE, TEST_DIR.resolve("title.episode.tsv"));
        when(fileDownloader.download(any())).thenReturn(filePaths);

        databaseUpdater.loadAllFiles();
        int epCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.episode");
        int showCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.show");
    }
}
