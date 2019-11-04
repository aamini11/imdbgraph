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
public class ImdbRatingsService {

    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    public ImdbRatingsService(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns every ratings for a show along with some basic information about
     * that show (title, year, etc...).
     *
     * @param showId The Imdb ID of the show to fetch ratings for.
     * @return POJO containing the basic show info and ratings
     */
    public RatingsGraph getAllShowRatings(String showId) {
        Optional<Show> show = getShow(showId);
        if (show.isEmpty()) {
            throw new InvalidParameterException("Invalid show ID");
        }
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        final String sql = "" +
                "SELECT episode_title,\n" +
                "       season_num,\n" +
                "       episode_num,\n" +
                "       imdb_rating,\n" +
                "       num_votes,\n" +
                "       COALESCE(episode_title, 'No title was found') AS primary_title\n" +
                "FROM imdb.episode\n" +
                "WHERE show_id = :showId AND episode_num > 0 AND season_num > 0\n" +
                "ORDER BY season_num, episode_num;";
        List<Episode> allEpisodeRatings = jdbc.query(sql, params, (rs, rowNum) -> {
            String title = rs.getString("episode_title");
            int season = rs.getInt("season_num");
            int episode = rs.getInt("episode_num");
            double imdbRating = rs.getDouble("imdb_rating");
            int numVotes = rs.getInt("num_votes");
            return new Episode(title, season, episode, imdbRating, numVotes);
        });
        return new RatingsGraph(show.get(), allEpisodeRatings);
    }

    /**
     * Method to search for a show using a search term provided by a user.
     *
     * @param searchTerm The search term to use
     * @return List of shows that match the search term
     */
    public List<Show> searchShows(String searchTerm) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("searchTerm", searchTerm);
        String sql = "" +
                "SELECT imdb_id,\n" +
                "       primary_title,\n" +
                "       start_year,\n" +
                "       end_year,\n" +
                "       imdb_rating,\n" +
                "       num_votes\n" +
                "FROM imdb.show\n" +
                "WHERE to_tsvector('english', primary_title) @@ plainto_tsquery('english', :searchTerm)\n" +
                "  AND EXISTS(SELECT * " +
                "             FROM imdb.show JOIN imdb.episode ON (imdb_id = show_id) " +
                "             WHERE episode.num_votes > 0)\n" +
                "ORDER BY num_votes DESC\n" +
                "LIMIT 50;";
        return jdbc.query(sql, params, (rs, rowNum) -> mapToShow(rs));
    }

    private Optional<Show> getShow(String showId) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        final String sql =
                "SELECT\n" +
                "  imdb_id," +
                "  primary_title, " +
                "  start_year," +
                "  end_year, " +
                "  imdb_rating as imdb_rating, " +
                "  num_votes as num_votes\n" +
                "FROM imdb.show\n" +
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
