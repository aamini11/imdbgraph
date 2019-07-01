package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbDao;
import org.aria.imdbgraph.imdb.Show;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class TestImdbDao {

    private static final String GAME_OF_THRONE_ID = "tt0944947";

    @Autowired
    private ImdbDao imdbDao;

    // TODO: add more tests...

    @Test
    public void testGameOfThrones() {
        List<Show> searchResults = imdbDao.searchShows("Game");

        boolean containsGOT = searchResults.stream()
                .anyMatch(show -> show.getImdbId().equals(GAME_OF_THRONE_ID));

        Assert.assertTrue(containsGOT);
    }
}
