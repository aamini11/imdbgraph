package org.aria.imdbgraph;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
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
    private Step scrapeTitles;

    @Autowired
    private Step scrapeEpisode;

    @Autowired
    private Step scrapeRatings;

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

    private void test(Step stepToTest) {
        JobExecution stepExecution = testLauncher.launchStep(stepToTest.getName());
        Assert.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
    }

    @Test
    public void testTitleScrapper() {
        test(scrapeTitles);
    }

    @Test
    public void testEpisodeScrapper() {
        test(scrapeEpisode);
    }

    @Test
    public void testRatingsScrapper() {
        test(scrapeRatings);
    }
}
