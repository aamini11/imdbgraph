package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbDao;
import org.aria.imdbgraph.imdb.Show;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBatchTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@TestConfiguration
public class ImdbgraphApplicationTests {

    @Autowired
    private ImdbDao imdbDao;

    @Autowired
    private JobLauncherTestUtils launcher;

    @Autowired
    private Job imdbScrappingJob;

    @Test
    public void testJob() {
        launchRatingsScrapper();
    }

    private void launchScrappingJob() throws Exception {
        launcher.launchJob();
    }

    private void launchTitlesScrapper() {
        launcher.launchStep("updateTitles");
    }

    private void launchEpisodeScrapper() {
        launcher.launchStep("updateEpisodes");
    }

    private void launchRatingsScrapper() {
        launcher.launchStep("updateRatings");
    }

    @TestConfiguration
    @EnableBatchProcessing
    public static class TestConfig {
        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils(Job imdbScrappingJob) {
            JobLauncherTestUtils launcher = new JobLauncherTestUtils();
            launcher.setJob(imdbScrappingJob);
            return launcher;
        }
    }

    @Test
    public void testSearchGOT() {
        List<Show> results = imdbDao.searchShows("Game");

        boolean containsGOT = false;
        for (Show show : results) {
            if (show.getTitle().equals("Game of Thrones")) {
                containsGOT = true;
                break;
            }
        }

        Assert.assertTrue(containsGOT);
    }
}
