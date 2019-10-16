package org.aria.imdbgraph.scrapper;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

import static org.aria.imdbgraph.scrapper.EpisodeScrapper.EpisodeRecord;

/**
 * Scrapper responsible for extracting all episode information.
 */
@Service
class EpisodeScrapper extends Scrapper<EpisodeRecord> {

    EpisodeScrapper(StepBuilderFactory stepBuilderFactory,
                    DataSource dataSource) {
        super(stepBuilderFactory, dataSource);
    }

    static final class EpisodeRecord {
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

    @Override
    void saveRecords(List<? extends EpisodeRecord> episodes) {
        String updateSql = "" +
                "INSERT INTO imdb.episode\n" +
                "SELECT val.show_id, val.episode_id, val.season, val.episode\n" +
                "FROM (VALUES (:showId, :episodeId, :season, :episode)) val(show_id, episode_id, season, episode)\n" +
                "         JOIN imdb.rateable_title ON (episode_id = rateable_title.imdb_id)\n" +
                "         JOIN imdb.show ON (show_id = show.imdb_id)\n" +
                "ON CONFLICT (episode_id) DO UPDATE\n" +
                "    SET show_id     = EXCLUDED.show_id," +
                "        season_num  = EXCLUDED.season_num,\n" +
                "        episode_num = EXCLUDED.episode_num;";
        SqlParameterSource[] params = episodes.stream()
                .map(record -> new MapSqlParameterSource()
                        .addValue("showId", record.showId)
                        .addValue("episodeId", record.episodeId)
                        .addValue("season", record.season)
                        .addValue("episode", record.episode))
                .toArray(SqlParameterSource[]::new);
        super.jdbc.batchUpdate(updateSql, params);
    }

    @Override
    EpisodeRecord mapLine(String line) {
        return new EpisodeRecord(line);
    }
}
