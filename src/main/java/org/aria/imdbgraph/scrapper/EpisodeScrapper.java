package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.function.Function;

import static org.aria.imdbgraph.scrapper.ImdbScrappingJob.CHUNK_SIZE;

/**
 * Service class to load episodes from flat files provided by IMDB into the database.
 */
final class EpisodeScrapper implements Step {

    private final Step delegateStep;

    EpisodeScrapper(NamedParameterJdbcOperations jdbc, StepBuilderFactory stepBuilderFactory, Resource resourceToRead) {
        this.delegateStep = createStep(jdbc, stepBuilderFactory, resourceToRead);
    }

    private static final class EpisodeRecord {
        final String episodeId;
        final String showId;
        final int season;
        final int episode;

        EpisodeRecord(String line) {
            String[] fields = line.split("\t");
            episodeId = fields[0];
            showId = fields[1];
            season = (fields[2].equals("\\N")) ? -1 : Integer.parseInt(fields[2]);
            episode = (fields[3].equals("\\N")) ? -1 : Integer.parseInt(fields[3]);
        }
    }

    private static Step createStep(NamedParameterJdbcOperations jdbc, StepBuilderFactory stepBuilderFactory, Resource resource) {
        return stepBuilderFactory.get("updateEpisodes")
                .<EpisodeRecord, EpisodeRecord>chunk(CHUNK_SIZE)
                .reader(createReader(resource))
                .processor((Function<EpisodeRecord, EpisodeRecord>) record -> {
                    if (record.episode != -1 && record.season != -1) return record;
                    else return null;
                })
                .writer(createWriter(jdbc))
                .build();
    }

    private static FlatFileItemReader<EpisodeRecord> createReader(Resource resource) {
        return new FlatFileItemReaderBuilder<EpisodeRecord>()
                .name("imdbEpisodeReader")
                .resource(resource)
                .linesToSkip(1)
                .lineMapper((line, lineNumber) -> new EpisodeRecord(line))
                .build();
    }

    private static JdbcBatchItemWriter<EpisodeRecord> createWriter(NamedParameterJdbcOperations jdbc) {
        //language=SQL
        final String updateSql = "" +
                "INSERT INTO imdb.episode(show_id, episode_id, season, episode)\n" +
                "VALUES (:showId, :episodeId, :season, :episode)\n" +
                "ON CONFLICT (episode_id) DO NOTHING;";

        var writer = new JdbcBatchItemWriterBuilder<EpisodeRecord>()
                .sql(updateSql)
                .namedParametersJdbcTemplate(jdbc)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("showId", record.showId)
                        .addValue("episodeId", record.episodeId)
                        .addValue("season", record.season)
                        .addValue("episode", record.episode))
                .assertUpdates(false)
                .build();
        writer.afterPropertiesSet();
        return writer;
    }

    @Override
    public String getName() {
        return delegateStep.getName();
    }

    @Override
    public boolean isAllowStartIfComplete() {
        return delegateStep.isAllowStartIfComplete();
    }

    @Override
    public int getStartLimit() {
        return delegateStep.getStartLimit();
    }

    @Override
    public void execute(StepExecution stepExecution) throws JobInterruptedException {
        delegateStep.execute(stepExecution);
    }
}
