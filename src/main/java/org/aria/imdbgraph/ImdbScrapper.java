package org.aria.imdbgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@EnableScheduling
public class ImdbScrapper {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);

    private final Job dailyScrapper;
    private final JobLauncher jobLauncher;

    @Autowired
    public ImdbScrapper(Job jobToLaunch, JobLauncher jobLauncher) {
        this.dailyScrapper = jobToLaunch;
        this.jobLauncher = jobLauncher;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void launchJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addDate("start-time", new Date())
                    .toJobParameters();
            jobLauncher.run(dailyScrapper, params);
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
            logger.error("Job failed", e);
        }
    }
}