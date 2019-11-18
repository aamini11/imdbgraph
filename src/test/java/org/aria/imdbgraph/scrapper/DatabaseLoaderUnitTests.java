package org.aria.imdbgraph.scrapper;


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

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DatabaseUpdater databaseUpdater;

    @MockBean
    private FileDownloader fileDownloader;

    @Test
    public void testSampleFiles() {
        Path root = Path.of("src/test/resources/data");

        Map<ImdbFile, Path> filePaths = new EnumMap<>(ImdbFile.class);
        filePaths.put(TITLES_FILE, root.resolve("title_sample.tsv"));
        filePaths.put(RATINGS_FILE, root.resolve("ratings_sample.tsv"));
        filePaths.put(EPISODES_FILE, root.resolve("episode_sample.tsv"));
        when(fileDownloader.download(any())).thenReturn(filePaths);

        databaseUpdater.loadAllFiles();
        int epCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.episode");
        int showCount = JdbcTestUtils.countRowsInTable(jdbc, "imdb.show");
        Assert.assertEquals(3, showCount);
        Assert.assertEquals(135, epCount);
    }
}
