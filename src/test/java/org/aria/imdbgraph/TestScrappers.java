package org.aria.imdbgraph;

import org.aria.imdbgraph.scrapper.EpisodeScrapper;
import org.aria.imdbgraph.scrapper.RatingScrapper;
import org.aria.imdbgraph.scrapper.Scrapper;
import org.aria.imdbgraph.scrapper.TitleScrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("dev")
@TestConfiguration
public class TestScrappers {

    @Autowired
    private TitleScrapper titleScrapper;

    @Autowired
    private EpisodeScrapper episodeScrapper;

    @Autowired
    private RatingScrapper ratingScrapper;

    @Autowired
    private JobLauncherTestUtils testLauncher;

    @Bean
    public JobLauncherTestUtils testLauncher(JobLauncher jobLauncher,
                                             JobRepository jobRepository,
                                             Job imdbScrappingJob) {
        JobLauncherTestUtils launcher = new JobLauncherTestUtils();
        launcher.setJobRepository(jobRepository);
        launcher.setJob(imdbScrappingJob);
        launcher.setJobLauncher(jobLauncher);
        return launcher;
    }

    private void testScrapper(Scrapper scrapperToTest) {
        JobExecution stepExecution = testLauncher.launchStep(scrapperToTest.getName());
        Assert.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
    }

    @Test
    public void testTitleScrapper() {
        testScrapper(titleScrapper);
    }

    @Test
    public void testEpisodeScrapper() {
        testScrapper(episodeScrapper);
    }

    @Test
    public void testRatingsScrapper() {
        testScrapper(ratingScrapper);
    }

    @Test
    public void testFullScrappingJob() throws Exception {
        JobExecution jobExecution = testLauncher.launchJob();
        Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }
}
