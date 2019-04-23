package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.aria.imdbgraph.scrapper.EpisodeScrapper.createEpisodeScrapper;
import static org.aria.imdbgraph.scrapper.FileService.ImdbFlatFile.*;
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
    public Job imdbScrapper(@Value("${DATA_DIR}") String dataDirectory) {
        final FileService fileService = new FileService(dataDirectory);

        final Step titleStep = createTitleScrapper(stepBuilder, fileService.toResource(TITLES_FILE), dataSource);
        final Step episodeStep = createEpisodeScrapper(stepBuilder, fileService.toResource(EPISODES_FILE), dataSource);
        final Step ratingsStep = createRatingsScrapper(stepBuilder, fileService.toResource(RATINGS_FILE), dataSource);

        return jobBuilder.get("imdbScrapper")
                .start(downloadFilesStep(fileService))
                .next(titleStep)
                .next(episodeStep)
                .next(ratingsStep)
                .build();
    }

    private Step downloadFilesStep(FileService fileService) {
        return stepBuilder.get("fileDownload")
                .tasklet((contribution, chunkContext) -> {
                    fileService.downloadAllFiles();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
