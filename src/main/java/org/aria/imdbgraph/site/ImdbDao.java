package org.aria.imdbgraph.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Service class that supports basic IMDB operations like getting ratings for a show.
 */
@Repository
public class ImdbDao {

    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    public ImdbDao(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns every ratings for a show along with some basic information about
     * that show (title, year, etc...).
     *
     * @param showId The Imdb ID of the show to fetch ratings for.
     * @return POJO containing the basic show info and ratings
     */
    @SuppressWarnings("SimplifyOptionalCallChains")
    public Ratings getAllShowRatings(String showId) {
        Optional<Show> showInfo = getShow(showId);
        if (!showInfo.isPresent()) {
            throw new InvalidParameterException("Invalid show ID");
        }
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        final String sql =
                "WITH rankings AS (\n" +
                "    SELECT episode_id, season_num, episode_num, imdb_rating, num_votes\n" +
                "    FROM imdb.episode\n" +
                "             LEFT JOIN imdb.rating ON (episode_id = imdb_id)\n" +
                "    WHERE show_id = :showId)\n" +
                "SELECT rankings.*,\n" +
                "       COALESCE(primary_title, 'No title was found') AS primary_title\n" +
                "FROM rankings LEFT JOIN imdb.title ON (imdb_id = episode_id)\n" +
                "WHERE title_type = 'tvEpisode' AND episode_num > 0 AND season_num > 0\n" +
                "ORDER BY season_num, episode_num ASC;";
        List<Episode> allEpisodeRatings = jdbc.query(sql, params, (rs, rowNum) -> {
            String title = rs.getString("primary_title");
            int season = rs.getInt("season_num");
            int episode = rs.getInt("episode_num");
            double imdbRating = rs.getDouble("imdb_rating");
            int numVotes = rs.getInt("num_votes");
            return new Episode(title, season, episode, imdbRating, numVotes);
        });
        return new Ratings(showInfo.get(), allEpisodeRatings);
    }

    /**
     * Method to search for a show using a search term provided by an end user.
     *
     * @param searchTerm The search term to use
     * @return List of shows that match the search term
     */
    public List<Show> searchShows(String searchTerm) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("searchTerm", searchTerm);
        String sql = "" +
                "WITH show_query AS (\n" +
                "    SELECT *, ts_rank(title_vec, title_query) as rank\n" +
                "    FROM (SELECT *,\n" +
                "                 to_tsvector('english', primary_title)  as title_vec,\n" +
                "                 plainto_tsquery('english', :searchTerm) as title_query\n" +
                "          FROM imdb.title) as query_ranking\n" +
                "    WHERE title_type = 'tvSeries'\n" +
                "      AND title_vec @@ title_query\n" +
                ")\n" +
                "SELECT imdb_id,\n" +
                "       primary_title,\n" +
                "       start_year,\n" +
                "       end_year,\n" +
                "       imdb_rating,\n" +
                "       num_votes\n" +
                "FROM show_query\n" +
                "         JOIN imdb.rating USING (imdb_id)\n" +
                "WHERE imdb_id IN (SELECT show_id AS d\n" +
                "                      FROM show_query\n" +
                "                               LEFT JOIN imdb.episode ON (imdb_id = show_id)\n" +
                "                               LEFT JOIN imdb.rating ON (episode_id = rating.imdb_id)\n" +
                "                      GROUP BY show_id\n" +
                "                      HAVING COUNT(episode_id) > 0\n" +
                "                         AND COALESCE(SUM(num_votes), 0) > 0)\n" +
                "ORDER BY rank DESC, num_votes DESC\n" +
                "LIMIT 50;";
        return jdbc.query(sql, params, (rs, rowNum) -> mapToShow(rs));
    }

    public Optional<Show> getShow(String showId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        final String sql =
                "SELECT\n" +
                "  imdb_id," +
                "  primary_title, " +
                "  start_year," +
                "  end_year, " +
                "  COALESCE(imdb_rating, 0) as imdb_rating, " +
                "  COALESCE(num_votes, 0) as num_votes\n" +
                "FROM imdb.title LEFT JOIN imdb.rating USING (imdb_id) " +
                "WHERE imdb_id = :showId";
        try {
            Show show = jdbc.queryForObject(sql, params, (rs, rowNum) -> mapToShow(rs));
            return Optional.ofNullable(show);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static Show mapToShow(ResultSet rs) throws SQLException {
        String imdbId = rs.getString("imdb_id");
        String title = rs.getString("primary_title");
        String startYear = rs.getString("start_year");
        String endYear = rs.getString("end_year");
        double imdbRating = rs.getDouble("imdb_rating");
        int numVotes = rs.getInt("num_votes");

        return new Show(imdbId, title, startYear, endYear, imdbRating, numVotes);
    }
}
