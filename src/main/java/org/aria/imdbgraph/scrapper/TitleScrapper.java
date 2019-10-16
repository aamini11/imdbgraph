package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.aria.imdbgraph.scrapper.TitleScrapper.TitleRecord;

/**
 * Class responsible for reading the file containing all the title information
 * for the shows and episodes
 */
@Repository
@EnableTransactionManagement
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
        List<SqlParameterSource> showParams = new ArrayList<>();
        List<SqlParameterSource> episodeParams = new ArrayList<>();
        for (TitleRecord r  : records) {
            SqlParameterSource param = new MapSqlParameterSource()
                    .addValue("imdbId", r.imdbId)
                    .addValue("primaryTitle", r.primaryTitle)
                    .addValue("titleType", r.titleType)
                    .addValue("startYear", r.startYear)
                    .addValue("endYear", r.endYear);
            if (r.titleType.equals("tvSeries")) {
                showParams.add(param);
            } else if (r.titleType.equals("tvEpisode")) {
                episodeParams.add(param);
            }
        }

        List<SqlParameterSource> allParams = new ArrayList<>();
        allParams.addAll(episodeParams);
        allParams.addAll(showParams);
        jdbc.batchUpdate("" +
                "INSERT INTO imdb.rateable_title(imdb_id)\n" +
                "VALUES (:imdbId)\n" +
                "ON CONFLICT DO NOTHING;",
                allParams.toArray(SqlParameterSource[]::new));
        jdbc.batchUpdate("" +
                "INSERT INTO imdb.show(imdb_id, primary_title, start_year, end_year)\n" +
                "VALUES (:imdbId, :primaryTitle, :startYear, :endYear)\n" +
                "ON CONFLICT (imdb_id) DO UPDATE\n" +
                "SET primary_title = EXCLUDED.primary_title,\n" +
                "    start_year    = EXCLUDED.start_year,\n" +
                "    end_year      = EXCLUDED.end_year;",
                showParams.toArray(SqlParameterSource[]::new));
        jdbc.batchUpdate("" +
                "INSERT INTO imdb.episode(episode_id, episode_title)\n" +
                "VALUES (:imdbId, :primaryTitle)\n" +
                "ON CONFLICT (episode_id) DO UPDATE\n" +
                "SET episode_title = EXCLUDED.episode_title;",
                episodeParams.toArray(SqlParameterSource[]::new));
    }
}