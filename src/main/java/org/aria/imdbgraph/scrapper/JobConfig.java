package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import java.net.URL;

import static org.aria.imdbgraph.scrapper.EpisodeScrapper.createEpisodeScrapper;
import static org.aria.imdbgraph.scrapper.FileUtil.*;
import static org.aria.imdbgraph.scrapper.RatingScrapper.createRatingsScrapper;
import static org.aria.imdbgraph.scrapper.TitleScrapper.createTitleScrapper;

@Configuration
@EnableBatchProcessing
public class JobConfig {

    static final int CHUNK_SIZE = 100;

    private final JobBuilderFactory jobBuilder;
    private final StepBuilderFactory stepBuilder;
    private final DataSource dataSource;

    @Autowired
    public JobConfig(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder, DataSource dataSource) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
        this.dataSource = dataSource;
    }

    @Bean
    public Job imdbScrapper() {
        final Resource titlesInput = new FileSystemResource(TITLES_FILE_PATH);
        final Resource episodesInput = new FileSystemResource(EPISODES_FILE_PATH);
        final Resource ratingsInput = new FileSystemResource(RATINGS_FILE_PATH);

        final Step titleStep = createTitleScrapper(stepBuilder, titlesInput, dataSource);
        final Step episodeStep = createEpisodeScrapper(stepBuilder, episodesInput, dataSource);
        final Step ratingsStep = createRatingsScrapper(stepBuilder, ratingsInput, dataSource);

        return jobBuilder.get("imdbScrapper")
                .start(downloadFilesStep())
                .next(titleStep)
                .next(episodeStep)
                .next(ratingsStep)
                .build();
    }

    private Step downloadFilesStep() {
        return stepBuilder.get("fileDownload")
                .tasklet((contribution, chunkContext) -> {
                    downloadAndUnzipFile(new URL(TITLES_FILE_URL), TITLES_FILE_PATH);
                    downloadAndUnzipFile(new URL(EPISODES_FILE_URL), EPISODES_FILE_PATH);
                    downloadAndUnzipFile(new URL(RATINGS_FILE_URL), RATINGS_FILE_PATH);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
