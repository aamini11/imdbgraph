package org.aria.imdbgraph.imdb;

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
     * Returns every ratings for a show along with some basic information about that show (title, year, etc...).
     *
     * @param showId The Imdb ID of the show to fetch ratings for.
     * @return POJO containing the basic show info and ratings
     */
    public Ratings getAllShowRatings(String showId) {
        Optional<Show> showInfo = getShow(showId);
        if (showInfo.isEmpty()) {
            throw new InvalidParameterException("Invalid show ID");
        }
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("showId", showId);
        final String sql = "" +
                "WITH rankings AS (\n" +
                "    SELECT *\n" +
                "    FROM imdb.episode LEFT JOIN imdb.rating ON (episode_id = imdb_id)\n" +
                "    WHERE show_id = :showId \n" +
                "    ORDER BY season, episode.episode ASC)\n" +
                "SELECT " +
                "  COALESCE(primary_title, 'No title was found') AS primary_title," +
                "  season," +
                "  episode," +
                "  imdb_rating, " +
                "  num_votes " +
                "FROM rankings LEFT JOIN imdb.title USING (imdb_id);";
        List<Episode> allEpisodeRatings = jdbc.query(sql, params, (rs, rowNum) -> {
            String title = rs.getString("primary_title");
            int season = rs.getInt("season");
            int episode = rs.getInt("episode");
            double imdbRating = rs.getDouble("imdb_rating");
            int numVotes = rs.getInt("num_votes");
            return new Episode(title, season, episode, imdbRating, numVotes);
        });
        return new Ratings(showInfo.get(), allEpisodeRatings);
    }

    /**
     * Search for shows using the OMDB api
     *
     * @param searchTerm The search term to use
     * @return Returns a list of shows matching the search term provided
     */
    public List<Show> searchShows(String searchTerm) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("searchTerm", searchTerm);
        String sql = "" +
                "SELECT imdb_id,\n" +
                "       primary_title,\n" +
                "       start_year,\n" +
                "       end_year,\n" +
                "       COALESCE(imdb_rating, 0) as imdb_rating,\n" +
                "       COALESCE(num_votes, 0)   as num_votes\n" +
                "FROM imdb.title JOIN imdb.rating USING (imdb_id)\n" +
                "WHERE title_type = 'tvSeries'\n" +
                "  AND plainto_tsquery(:searchTerm) @@ to_tsvector('english', primary_title)\n" +
                "ORDER BY ts_rank_cd(to_tsvector('english', primary_title), plainto_tsquery(:searchTerm)) DESC, num_votes DESC\n" +
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
                "  COALESCE(imdb_rating, 0) as imdb_rating, " +
                "  COALESCE(num_votes, 0) as num_votes\n" +
                "FROM imdb.title LEFT JOIN imdb.rating USING (imdb_id) " +
                "WHERE imdb_id = :showId";
        try {
            return jdbc.queryForObject(sql, params, (rs, rowNum) -> Optional.of(mapToShow(rs)));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Show mapToShow(ResultSet rs) throws SQLException {
        String imdbId = rs.getString("imdb_id");
        String title = rs.getString("primary_title");
        String startYear = rs.getString("start_year");
        String endYear = rs.getString("end_year");
        double imdbRating = rs.getDouble("imdb_rating");
        int numVotes = rs.getInt("num_votes");

        return new Show(imdbId, title, startYear, endYear, imdbRating, numVotes);
    }
}
