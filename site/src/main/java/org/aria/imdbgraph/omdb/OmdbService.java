package org.aria.imdbgraph.omdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.aria.imdbgraph.omdb.ShowRatings.SeasonRating;

/*
 Class that supports basic OMDB operations such as getting the total number of seasons a TV series has.
 */
@Service
public class OmdbService {

    private static final String BASE_URL = "https://www.omdbapi.com";

    private final String apiKey;
    private final RestTemplate restTemplate;

    @Autowired
    public OmdbService(@Value("${omdb.apikey}") String apiKey, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    private static final class OmdbResponse {
        private final int numSeasons;
        private final String title;

        @JsonCreator
        public OmdbResponse(@JsonProperty("totalSeasons") int numSeaons,
                            @JsonProperty("title") String title) {
            this.numSeasons = numSeaons;
            this.title = title;
        }

        String getTitle() {
            return title;
        }

        int getNumSeasons() {
            return numSeasons;
        }
    }

    private OmdbResponse getShowInfo(String showId) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("apikey", apiKey)
                .queryParam("i", showId)
                .toUriString();
        OmdbResponse response = restTemplate.getForObject(uri, OmdbResponse.class);
        Objects.requireNonNull(response);
        return response;
    }

    private SeasonRating getSeason(String showId, int season) {
        String uri = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .queryParam("apikey", apiKey)
                .queryParam("i", showId)
                .queryParam("Season", season)
                .toUriString();
        return restTemplate.getForObject(uri, SeasonRating.class);
    }

    public ShowRatings getShowRating(String showId) {
        OmdbResponse showInfo = getShowInfo(showId);
        List<SeasonRating> allSeasons = new ArrayList<>();
        for (int season = 1; season <= showInfo.getNumSeasons(); season++) {
            allSeasons.add(getSeason(showId, season));
        }
        return new ShowRatings(allSeasons, showInfo.getTitle());
    }
}
