package org.aria.imdbgraph.scrapper;

import org.aria.imdbgraph.scrapper.EpisodeScrapper.EpisodeRecord;
import org.aria.imdbgraph.scrapper.RatingScrapper.RatingRecord;
import org.aria.imdbgraph.scrapper.TitleScrapper.TitleRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
    public JobConfig(JobBuilderFactory jobBuilder,
                     StepBuilderFactory stepBuilder,
                     ImdbFileDownloader fileDownloader) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
        this.fileDownloader = fileDownloader;
    }

    @Bean
    public Job imdbScrappingJob(Step downloadFiles,
                                Step scrapeTitles,
                                Step scrapeEpisode,
                                Step scrapeRatings) {
        return jobBuilder.get("imdbScrappingJob")
                .incrementer(new RunIdIncrementer())
                .start(downloadFiles)
                .next(scrapeTitles)
                .next(scrapeEpisode)
                .next(scrapeRatings)
                .build();
    }

    @Bean
    public Step scrapeRatings(Scrapper<RatingRecord> ratingsScrapper) {
        Path ratingsFile = fileDownloader.getPath(RATINGS_FILE);
        return ratingsScrapper.createStep(ratingsFile);
    }

    @Bean
    public Step scrapeTitles(Scrapper<TitleRecord> titleScrapper) {
        Path titleFile = fileDownloader.getPath(TITLES_FILE);
        return titleScrapper.createStep(titleFile);
    }

    @Bean
    public Step scrapeEpisode(Scrapper<EpisodeRecord> episodeScrapper) {
        Path episodeFile = fileDownloader.getPath(EPISODES_FILE);
        return episodeScrapper.createStep(episodeFile);
    }

    @Bean
    public Step downloadFiles(ImdbFileDownloader fileService) {
        return stepBuilder.get("fileDownload")
                .tasklet((contribution, chunkContext) -> {
                    fileService.downloadAllFiles();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}

