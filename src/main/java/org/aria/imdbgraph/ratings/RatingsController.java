package org.aria.imdbgraph.ratings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Controller
class RatingsController {

    private final ImdbRatingsService imdbRatingsService;

    @Autowired
    RatingsController(ImdbRatingsService imdbRatingsService) {
        this.imdbRatingsService = imdbRatingsService;
    }

    @GetMapping(value = "/")
    public String index() {
        return "search_results.html";
    }

    @GetMapping(value = "/ratings/{showId}")
    public String getRating(@PathVariable(value = "showId") String showId, Model model) {
        Optional<RatingsGraph> ratings = imdbRatingsService.getAllShowRatings(showId);
        if (ratings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found");
        }
        model.addAttribute("ratingsData", ratings.get());
        return "rating_page.html";
    }

    @GetMapping(value = "/search")
    public String search(@RequestParam(value="q") String searchTerm, Model model) {
        List<Show> shows = imdbRatingsService.searchShows(searchTerm);
        model.addAttribute("results", shows);
        return "search_results.html";
    }
}
