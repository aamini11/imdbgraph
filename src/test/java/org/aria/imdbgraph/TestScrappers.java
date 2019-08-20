package org.aria.imdbgraph;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("dev")
@TestConfiguration
public class TestScrappers implements IntegrationTests {

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

    @Bean
    public JobOperator jobOperator(final JobLauncher jobLauncher, final JobRepository jobRepository,
                                   final JobRegistry jobRegistry, final JobExplorer jobExplorer) {
        final SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobExplorer(jobExplorer);
        return jobOperator;
    }

    @Bean
    public JobExplorer jobExplorer(final DataSource dataSource) throws Exception {
        final JobExplorerFactoryBean bean = new JobExplorerFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTablePrefix("BATCH_");
        bean.setJdbcOperations(new JdbcTemplate(dataSource));
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    private void test(Step stepToTest) {
        JobExecution stepExecution = testLauncher.launchStep(stepToTest.getName());
        Assert.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
    }

    @Test
    public void testAllScrappers() {
        test(scrapeTitles);
        test(scrapeRatings);
        test(scrapeEpisode);
    }
}
