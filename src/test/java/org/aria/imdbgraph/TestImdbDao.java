package org.aria.imdbgraph;

import org.aria.imdbgraph.site.ImdbDao;
import org.aria.imdbgraph.site.Ratings;
import org.aria.imdbgraph.site.Show;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class TestImdbDao {

    private static final String GAME_OF_THRONE_ID = "tt0944947";

    @Autowired
    private ImdbDao imdbDao;

    @Test
    public void testGameOfThrones() {
        List<Show> searchResults = imdbDao.searchShows("Game");

        List<Show> matches = searchResults.stream()
                .filter(show -> show.getImdbId().equals(GAME_OF_THRONE_ID))
                .collect(Collectors.toList());

        Assert.assertEquals(1, matches.size());
        Show match = matches.get(0);

        Assert.assertEquals("Game of Thrones", match.getTitle());
        Assert.assertEquals(GAME_OF_THRONE_ID, match.getImdbId());

        Ratings gotRatings = imdbDao.getAllShowRatings(match.getImdbId());
        int numSeasons = gotRatings.getAllRatings().keySet().size();
        Assert.assertEquals(8, numSeasons);
    }
}
