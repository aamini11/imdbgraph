package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

import static org.aria.imdbgraph.scrapper.RatingScrapper.RatingRecord;

/**
 * Class responsible for extracting all the ratings data.
 */
@Service
class RatingScrapper extends Scrapper<RatingRecord> {

    @Autowired
    RatingScrapper(StepBuilderFactory stepBuilderFactory,
                          DataSource dataSource) {
        super(stepBuilderFactory, dataSource);
    }

    static final class RatingRecord {
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

    @Override
    RatingRecord mapLine(String line) {
        return new RatingRecord(line);
    }

    @Override
    void saveRecords(List<? extends RatingRecord> records) {
        final String updateSql = "" +
                "INSERT INTO imdb.rating(imdb_id, imdb_rating, num_votes)\n" +
                "VALUES (:imdbId, :imdbRating, :numVotes)\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "SET\n" +
                "  imdb_rating = :imdbRating," +
                "  num_votes = :numVotes;";
        SqlParameterSource[] params = records.stream()
                .map(record -> new MapSqlParameterSource()
                        .addValue("imdbId", record.imdbId)
                        .addValue("imdbRating", record.imdbRating)
                        .addValue("numVotes", record.numVotes))
                .toArray(MapSqlParameterSource[]::new);
        super.jdbc.batchUpdate(updateSql, params);
    }
}
