package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static org.aria.imdbgraph.scrapper.FileUtil.*;

@EnableBatchProcessing
@Configuration
public class ImdbScrappingJob {

    static final int CHUNK_SIZE = 100;

    private final JobBuilderFactory jobBuilderFactory;
    private final Step episodeScrapper;
    private final Step ratingScrapper;
    private final Step titleScrapper;

    public ImdbScrappingJob(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder, NamedParameterJdbcOperations jdbc) {
        this.jobBuilderFactory = jobBuilder;

        this.episodeScrapper = new EpisodeScrapper(jdbc, stepBuilder, openUrl(EPISODES_FILE_URL));
        this.ratingScrapper = new RatingScrapper(jdbc, stepBuilder, openUrl(RATINGS_FILE_URL));
        this.titleScrapper = new TitleScrapper(jdbc, stepBuilder, openUrl(SHOW_FILE_URL));
    }

    @Bean
    public Job imdbScrapper() {
        return jobBuilderFactory.get("imdbScrapper")
                .start(episodeScrapper)
                .next(ratingScrapper)
                .next(titleScrapper)
                .build();
    }
}
