package org.aria.imdbgraph.ratings;

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
 * Service class that supports basic IMDB operations involving ratings data.
 * Operations like searching for shows and getting episode ratings for TV shows.
 */
@Repository
public class ImdbRatingsService {

    private final NamedParameterJdbcOperations jdbc;

    @Autowired
    public ImdbRatingsService(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Method that returns all ratings data for a specific show.
     *
     * @param showId The Imdb ID of the show to fetch ratings for.
     * @return A {@code RatingsGraph} object containing all the show information
     * and episode ratings.
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
                "WHERE show_id = :showId\n" +
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
     * Search method used by users to look for TV shows.
     *
     * @param searchQuery The search query provided by the user.
     * @return List of possible shows that match the search query.
     */
    public List<Show> searchShows(String searchQuery) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("searchTerm", searchQuery);
        String sql = "" +
                "SELECT imdb_id,\n" +
                "       primary_title,\n" +
                "       start_year,\n" +
                "       end_year,\n" +
                "       imdb_rating,\n" +
                "       num_votes\n" +
                "FROM imdb.show\n" +
                "WHERE :searchTerm <% primary_title\n" +
                "  AND imdb_id IN (SELECT show_id FROM imdb.valid_show)\n" +
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
                "  imdb_rating, " +
                "  num_votes\n" +
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
