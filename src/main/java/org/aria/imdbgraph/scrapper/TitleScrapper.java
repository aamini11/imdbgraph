package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

import static org.aria.imdbgraph.scrapper.TitleScrapper.TitleRecord;

/**
 * Class responsible for reading the file containing all the title information
 * for the shows and episodes
 */
@Service
class TitleScrapper extends Scrapper<TitleRecord> {

    @Autowired
    TitleScrapper(StepBuilderFactory stepBuilderFactory,
                         DataSource dataSource) {
        super(stepBuilderFactory, dataSource);
    }

    static final class TitleRecord {
        final String imdbId;
        final String titleType;
        final String primaryTitle;
        final String originalTitle;
        final String startYear;
        final String endYear;

        TitleRecord(String line) {
            String[] fields = line.split("\t");
            imdbId = fields[0];
            titleType = fields[1];
            primaryTitle = fields[2];
            originalTitle = fields[3];
            startYear = (fields[5].equals("\\N")) ? null : fields[5];
            endYear = (fields[6].equals("\\N")) ? null : fields[6];
        }
    }

    @Override
    TitleRecord mapLine(String line) {
        return new TitleRecord(line);
    }

    @Override
    void saveRecords(List<? extends TitleRecord> records) {
        SqlParameterSource[] showParams = records.stream()
                .map(record -> new MapSqlParameterSource()
                        .addValue("imdbId", record.imdbId)
                        .addValue("primaryTitle", record.primaryTitle)
                        .addValue("titleType", record.titleType)
                        .addValue("startYear", record.startYear)
                        .addValue("endYear", record.endYear))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate("" +
                "INSERT INTO imdb.title(imdb_id, primary_title, title_type, start_year, end_year)\n" +
                "VALUES (:imdbId, :primaryTitle, :titleType, :startYear, :endYear)\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "SET \n" +
                "  primary_title = :primaryTitle," +
                "  title_type = :titleType," +
                "  start_year = :startYear," +
                "  end_year = :endYear;", showParams);
    }
}