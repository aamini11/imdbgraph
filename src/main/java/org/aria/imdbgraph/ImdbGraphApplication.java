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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class ImdbGraphApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);

    private final Job imdbScrapper;
    private final JobLauncher jobLauncher;

    @Autowired
    public ImdbGraphApplication(Job imdbScrapper, JobLauncher jobLauncher) {
        this.imdbScrapper = imdbScrapper;
        this.jobLauncher = jobLauncher;
    }

    public static void main(String[] args) {
        SpringApplication.run(ImdbGraphApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Set<CommandLineOption> options = CommandLineOption.fromArgs(args);

        if (options.contains(CommandLineOption.LAUNCH_JOB)) {
            runDailyJob();
        }
    }

    private enum CommandLineOption {
        LAUNCH_JOB;

        static Set<CommandLineOption> fromArgs(String[] args) {
            return Arrays.stream(args)
                    .map(CommandLineOption::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CommandLineOption.class)));
        }
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addDate("start-time", new Date())
                    .toJobParameters();
            jobLauncher.run(imdbScrapper, params);
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
            logger.error("Job failed", e);
        }
    }
}
