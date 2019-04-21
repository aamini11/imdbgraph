package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.aria.imdbgraph.scrapper.EpisodeScrapper.createEpisodeScrapper;
import static org.aria.imdbgraph.scrapper.FileUtil.*;
import static org.aria.imdbgraph.scrapper.RatingScrapper.createRatingsScrapper;
import static org.aria.imdbgraph.scrapper.TitleScrapper.createTitleScrapper;

@Configuration
@EnableBatchProcessing
public class JobConfig implements CommandLineRunner {

    static final int CHUNK_SIZE = 100;

    private final JobBuilderFactory jobBuilder;
    private final StepBuilderFactory stepBuilder;
    private final DataSource dataSource;
    private final JobLauncher jobLauncher;

    @Autowired
    public JobConfig(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder, DataSource dataSource, JobLauncher jobLauncher) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
        this.dataSource = dataSource;
        this.jobLauncher = jobLauncher;
    }

    @Override
    public void run(String... args) throws Exception {
        Set<CommandLineOption> options = CommandLineOption.fromArgs(args);

        if (options.contains(CommandLineOption.LAUNCH_JOB)) {
            JobParameters params =  new JobParametersBuilder()
                    .addDate("currentTime", new Date())
                    .toJobParameters();

            jobLauncher.run(imdbScrapper(), params);
        }
    }

    @Bean
    public Job imdbScrapper() throws MalformedURLException {
        final Resource episodesInput = new ZippedResource(new UrlResource(EPISODES_FILE_URL));
        final Resource ratingsInput = new ZippedResource(new UrlResource(RATINGS_FILE_URL));
        final Resource showTitlesInput = new ZippedResource(new UrlResource(SHOW_FILE_URL));

        final Step episodeStep = createEpisodeScrapper(stepBuilder, episodesInput, dataSource);
        final Step ratingsStep = createRatingsScrapper(stepBuilder, ratingsInput, dataSource);
        final Step titleStep = createTitleScrapper(stepBuilder, showTitlesInput, dataSource);

        return jobBuilder.get("imdbScrapper")
                .start(titleStep)
                .next(episodeStep)
                .next(ratingsStep)
                .build();
    }


    private enum CommandLineOption {
        LAUNCH_JOB;

        static Set<CommandLineOption> fromArgs(String[] args) {
            return Arrays.stream(args)
                    .map(CommandLineOption::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(CommandLineOption.class)));
        }
    }
}
