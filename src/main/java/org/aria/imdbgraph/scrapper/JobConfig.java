package org.aria.imdbgraph.scrapper;

import org.aria.imdbgraph.scrapper.EpisodeScrapper.EpisodeRecord;
import org.aria.imdbgraph.scrapper.RatingScrapper.RatingRecord;
import org.aria.imdbgraph.scrapper.TitleScrapper.TitleRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

import static org.aria.imdbgraph.scrapper.ImdbFileDownloader.ImdbFlatFile.*;

@Configuration
@EnableBatchProcessing
public class JobConfig {

    private final JobBuilderFactory jobBuilder;
    private final StepBuilderFactory stepBuilder;
    private final ImdbFileDownloader fileDownloader;

    @Autowired
    JobConfig(JobBuilderFactory jobBuilder,
              StepBuilderFactory stepBuilder,
              ImdbFileDownloader fileDownloader) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
        this.fileDownloader = fileDownloader;
    }

    @Bean
    public Job imdbScrappingJob(Step downloadFilesStep,
                                Scrapper<TitleRecord> titleScrapper,
                                Scrapper<EpisodeRecord> episodeScrapper,
                                Scrapper<RatingRecord> ratingsScrapper) {
        Path titleFile = fileDownloader.getPath(TITLES_FILE);
        Step titleStep = titleScrapper.createStep(titleFile);

        Path episodeFile = fileDownloader.getPath(EPISODES_FILE);
        Step episodeStep = episodeScrapper.createStep(episodeFile);

        Path ratingsFile = fileDownloader.getPath(RATINGS_FILE);
        Step ratingsStep = ratingsScrapper.createStep(ratingsFile);

        return jobBuilder.get("imdbScrappingJob")
                .start(downloadFilesStep)
                .next(titleStep)
                .next(episodeStep)
                .next(ratingsStep)
                .build();
    }

    @Bean
    public Step downloadFilesStep(ImdbFileDownloader fileService) {
        return stepBuilder.get("fileDownload")
                .tasklet((contribution, chunkContext) -> {
                    fileService.downloadAllFiles();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}

