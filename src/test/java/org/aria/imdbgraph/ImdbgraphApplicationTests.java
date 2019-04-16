package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbDao;
import org.aria.imdbgraph.imdb.Show;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("dev")
public class ImdbgraphApplicationTests {

    @Autowired
    private ImdbDao imdbDao;

    @Autowired
    private JobLauncherTestUtils launcher;

    @Test
    public void testJob() throws Exception {
        launcher.launchStep("updateTitles");
    }

    @Test
    public void testSearch() {
        List<Show> results = imdbDao.searchShows("Game");
        Assert.assertEquals("Game of Thrones", results.get(0).getTitle());
    }
}
