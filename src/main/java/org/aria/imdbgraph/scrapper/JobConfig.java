package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.net.MalformedURLException;

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
    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    public JobConfig(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder, NamedParameterJdbcOperations jdbc) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
        this.jdbc = jdbc; }

    @Bean
    public Job imdbScrapper() throws MalformedURLException {
        final Resource episodesInput = new UnzippedResource(new UrlResource(EPISODES_FILE_URL));
        final Resource ratingsInput = new UnzippedResource(new UrlResource(RATINGS_FILE_URL));
        final Resource showTitlesInput = new UnzippedResource(new UrlResource(SHOW_FILE_URL));

        final Step episodeStep = createEpisodeScrapper(stepBuilder, episodesInput, jdbc);
        final Step ratingsStep = createRatingsScrapper(stepBuilder, ratingsInput, jdbc);
        final Step titleStep = createTitleScrapper(stepBuilder, showTitlesInput, jdbc);

        return jobBuilder.get("imdbScrapper")
                .start(titleStep)
                .next(episodeStep)
                .next(ratingsStep)
                .build();
    }
}
