package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import static org.aria.imdbgraph.scrapper.FileUtil.*;

@Configuration
@EnableBatchProcessing
public class JobConfig {

    static final int CHUNK_SIZE = 100;

    private final JobBuilderFactory jobBuilderFactory;
    private final Step episodeScrapper;
    private final Step ratingScrapper;
    private final Step titleScrapper;

    @Autowired
    public JobConfig(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder, NamedParameterJdbcOperations jdbc) {
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
