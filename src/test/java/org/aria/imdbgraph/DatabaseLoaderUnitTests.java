package org.aria.imdbgraph;


import org.aria.imdbgraph.scrapper.DatabaseUpdater;
import org.aria.imdbgraph.scrapper.FileDownloader;
import org.aria.imdbgraph.site.ImdbRatingsService;
import org.aria.imdbgraph.site.RatingsGraph;
import org.aria.imdbgraph.site.Show;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.aria.imdbgraph.scrapper.FileDownloader.ImdbFile.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class DatabaseLoaderUnitTests {

    private static final Path TEST_DIR = Paths.get("C:\\Users\\Aria\\IdeaProjects\\imdbgraph\\src\\test\\resources\\data");

    private static final String GAME_OF_THRONE_ID = "tt0944947";
    private static final String AVATAR_ID = "tt0417299";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ImdbRatingsService imdbRatingsService;

    private void clearTables() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DELETE FROM imdb.episode");
        jdbc.execute("DELETE FROM imdb.show");
        jdbc.execute("DELETE FROM imdb.rateable_title");
    }

    @Test
    public void testSampleFiles() {
        clearTables();

        Map<FileDownloader.ImdbFile, Path> testFiles = new EnumMap<>(FileDownloader.ImdbFile.class);
        testFiles.put(TITLES_FILE, TEST_DIR.resolve("title_sample.tsv"));
        testFiles.put(RATINGS_FILE, TEST_DIR.resolve("ratings_sample.tsv"));
        testFiles.put(EPISODES_FILE, TEST_DIR.resolve("episode_sample.tsv"));

        FileDownloader f = mock(FileDownloader.class);
        when(f.download(any())).thenReturn(testFiles);

        DatabaseUpdater d = new DatabaseUpdater(dataSource, f);
        d.loadAllFiles();

        List<Show> shows = imdbRatingsService.searchShows("Avatar");
        boolean hasAvatar = false;
        for (Show show : shows) {
            if (show.getImdbId().equals(AVATAR_ID)) {
                hasAvatar = true;
                break;
            }
        }
        Assert.assertTrue(hasAvatar);

        RatingsGraph graph = imdbRatingsService.getAllShowRatings(AVATAR_ID);
        Assert.assertEquals(3, graph.getAllEpisodeRatings().size());
    }

    @Test
    public void testRealFiles() {
        Map<FileDownloader.ImdbFile, Path> testFiles = new EnumMap<>(FileDownloader.ImdbFile.class);
        testFiles.put(TITLES_FILE, TEST_DIR.resolve("title.basics.tsv"));
        testFiles.put(RATINGS_FILE, TEST_DIR.resolve("title.ratings.tsv"));
        testFiles.put(EPISODES_FILE, TEST_DIR.resolve("title.episode.tsv"));

        FileDownloader f = mock(FileDownloader.class);
        when(f.download(any())).thenReturn(testFiles);

        DatabaseUpdater d = new DatabaseUpdater(dataSource, f);
        d.loadAllFiles();
    }
}
