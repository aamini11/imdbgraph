package org.aria.imdbgraph;


import org.aria.imdbgraph.site.EpisodeRatings;
import org.aria.imdbgraph.site.ImdbDao;
import org.aria.imdbgraph.site.Show;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("dev")
public class TestImdbDao {

    private static final String GAME_OF_THRONE_ID = "tt0944947";
    private static final String AVATAR_ID = "tt0417299";

    @Autowired
    private Step scrapeTitles;

    @Autowired
    private Step scrapeEpisode;

    @Autowired
    private Step scrapeRatings;

    @Autowired
    private JobLauncherTestUtils testLauncher;

    @Autowired
    private ImdbDao imdbDao;

    public void test(Step stepToTest) {
        JobExecution stepExecution = testLauncher.launchStep(stepToTest.getName());
        Assert.assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
    }

    @Test
    public void testAllScrappers() {
        test(scrapeTitles);
        test(scrapeRatings);
        test(scrapeEpisode);
    }

    @Test
    public void searchGOT() {
        testSearch("Game", "Game of Thrones", GAME_OF_THRONE_ID);
    }

    @Test
    public void searchAvatar() {
        testSearch("Avatar", "Avatar: The Last Airbender", AVATAR_ID);
    }

    @Test
    public void validateGOTRatings() {
        validateRatings(GAME_OF_THRONE_ID, 8);
    }

    @Test
    public void validateAvatarRatings() {
        validateRatings(AVATAR_ID, 3);
    }

    private void testSearch(String searchTerm, String expectedTitle, String imdbId) {
        List<Show> searchResults = imdbDao.searchShows(searchTerm);
        List<Show> matches = searchResults.stream()
                .filter(show -> show.getImdbId().equals(imdbId))
                .collect(Collectors.toList());
        Assert.assertEquals(1, matches.size());
        Show match = matches.get(0);

        Assert.assertEquals(expectedTitle, match.getTitle());
        Assert.assertEquals(imdbId, match.getImdbId());
    }

    private void validateRatings(String imdbId, int expectedNumSeasons) {
        Optional<Show> result = imdbDao.getShow(imdbId);
        Assert.assertTrue(result.isPresent());

        Show avatar = result.get();
        EpisodeRatings ratings = imdbDao.getAllShowRatings(avatar.getImdbId());
        int numSeasons = ratings.getAllEpisodeRatings().keySet().size();
        Assert.assertEquals(expectedNumSeasons, numSeasons);
    }
}
