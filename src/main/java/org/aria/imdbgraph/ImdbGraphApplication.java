package org.aria.imdbgraph;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@SpringBootApplication
public class ImdbGraphApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImdbGraphApplication.class, args);
    }

    private final Job imdbScrapper;
    private final JobLauncher jobLauncher;

    @Autowired
    public ImdbGraphApplication(Job imdbScrapper, JobLauncher jobLauncher) {
        this.imdbScrapper = imdbScrapper;
        this.jobLauncher = jobLauncher;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void dailyJob() {
        try {
            jobLauncher.run(imdbScrapper, new JobParametersBuilder().addDate("start-time", new Date()).toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
            e.printStackTrace();
        }
    }
}
