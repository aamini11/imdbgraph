package org.aria.imdbgraph.imdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping
public class RatingController {

    private final ImdbDao imdbService;

    @Autowired
    public RatingController(ImdbDao imdbService) {
        this.imdbService = imdbService;
    }

    @GetMapping(value = "/")
    public String index() {
        return "search_results.html";
    }

    @GetMapping(value = "/ratings/{showId}")
    public String getRating(@PathVariable(value = "showId") String showId, Model model) {
        model.addAttribute("ratingsData", imdbService.getAllShowRatings(showId));
        return "rating_page.html";
    }

    @GetMapping(value = "/search")
    public String search(@RequestParam(value="q") String searchTerm, Model model) {
        List<Show> shows = imdbService.searchShows(searchTerm);
        model.addAttribute("results", shows);
        return "search_results.html";
    }
}
