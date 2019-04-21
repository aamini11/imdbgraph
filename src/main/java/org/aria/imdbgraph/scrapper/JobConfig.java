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

import javax.sql.DataSource;
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
    private final DataSource dataSource;

    @Autowired
    public JobConfig(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder, DataSource dataSource) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
        this.dataSource = dataSource;
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
}
