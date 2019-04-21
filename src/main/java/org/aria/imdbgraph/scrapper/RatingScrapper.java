package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;

import static org.aria.imdbgraph.scrapper.JobConfig.CHUNK_SIZE;

/**
 * Static utility class to create the ratings scrapping step which is used in the job configuration.
 */
class RatingScrapper {

    private RatingScrapper() {}

    private static final class RatingRecord {
        final String imdbId;
        final double imdbRating;
        final int numVotes;

        RatingRecord(String line) {
            String[] fields = line.split("\t");
            imdbId = fields[0];
            imdbRating = Double.parseDouble(fields[1]);
            numVotes = Integer.parseInt(fields[2]);
        }
    }

    static Step createRatingsScrapper(StepBuilderFactory stepBuilder, Resource input, DataSource dataSource) {
        return stepBuilder.get("updateRatings")
                .<RatingRecord, RatingRecord>chunk(CHUNK_SIZE)
                .reader(createReader(input))
                .writer(createWriter(dataSource))
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

    private static JdbcBatchItemWriter<RatingRecord> createWriter(DataSource dataSource) {
        //language=SQL
        final String updateSql = "" +
                "INSERT INTO imdb.rating(imdb_id, imdb_rating, num_votes)\n" +
                "VALUES (:imdbId, :imdbRating, :numVotes)\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "SET\n" +
                "  imdb_rating = :imdbRating," +
                "  num_votes = :numVotes;";

        var writer = new JdbcBatchItemWriterBuilder<RatingRecord>()
                .sql(updateSql)
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(record -> new MapSqlParameterSource()
                        .addValue("imdbId", record.imdbId)
                        .addValue("imdbRating", record.imdbRating)
                        .addValue("numVotes", record.numVotes))
                .build();
        writer.afterPropertiesSet();
        return writer;
    }
}
