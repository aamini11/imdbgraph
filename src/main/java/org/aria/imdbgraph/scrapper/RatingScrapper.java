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

import static org.aria.imdbgraph.scrapper.ImdbScrappingJob.CHUNK_SIZE;

final class RatingScrapper implements Step {

    private final Step delegateStep;

    RatingScrapper(NamedParameterJdbcOperations jdbc, StepBuilderFactory stepBuilderFactory, Resource resourceToRead) {
        this.delegateStep = createStep(jdbc, stepBuilderFactory, resourceToRead);
    }

    private static class RatingRecord {
        final String episodeId;
        final double imdbRating;
        final int numVotes;

        RatingRecord(String line) {
            String[] fields = line.split("\t");
            episodeId = fields[0];
            imdbRating = Double.parseDouble(fields[1]);
            numVotes = Integer.parseInt(fields[2]);
        }
    }

    private static Step createStep(NamedParameterJdbcOperations jdbc, StepBuilderFactory stepBuilderFactory, Resource resourceToRead) {
        return stepBuilderFactory.get("updateRatings")
                .<RatingRecord, RatingRecord>chunk(CHUNK_SIZE)
                .reader(createReader(resourceToRead))
                .writer(createWriter(jdbc))
                .build();
    }

    private static FlatFileItemReader<RatingRecord> createReader(Resource resource) {
        return new FlatFileItemReaderBuilder<RatingRecord>()
                .name("imdbRatingReader")
                .resource(resource)
                .linesToSkip(1)
                .lineMapper((line, lineNum) -> new RatingRecord(line))
                .build();
    }

    private static JdbcBatchItemWriter<RatingRecord> createWriter(NamedParameterJdbcOperations jdbc) {
        //language=SQL
        final String updateSql =
                "INSERT INTO imdb.rating(imdb_id, imdb_rating, num_votes)\n" +
                "VALUES (:episodeId, :imdbRating, :numVotes)\n" +
                "ON CONFLICT DO NOTHING;";

        var writer = new JdbcBatchItemWriterBuilder<RatingRecord>()
                .sql(updateSql)
                .namedParametersJdbcTemplate(jdbc)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("episodeId", record.episodeId)
                        .addValue("imdbRating", record.imdbRating)
                        .addValue("numVotes", record.numVotes))
                .build();
        writer.afterPropertiesSet();
        return writer;
    }

    public String getName() {
        return delegateStep.getName();
    }

    public boolean isAllowStartIfComplete() {
        return delegateStep.isAllowStartIfComplete();
    }

    public int getStartLimit() {
        return delegateStep.getStartLimit();
    }

    public void execute(StepExecution stepExecution) throws JobInterruptedException {
        delegateStep.execute(stepExecution);
    }
}
