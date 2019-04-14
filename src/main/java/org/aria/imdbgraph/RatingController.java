package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RatingController {

    private final ImdbDao imbdService;

    @Autowired
    public RatingController(ImdbDao imbdService) {
        this.imbdService = imbdService;
    }

    @GetMapping(value = "/")
    public String index() {
        return "search_results.html";
    }

    @GetMapping(value = "/ratings")
    public String getRating(@RequestParam(value="id") String showId, Model model) {
        model.addAttribute("ratingsData", imbdService.getAllShowRatings(showId));
        return "rating_page.html";
    }

    @GetMapping(value = "/search")
    public String search(@RequestParam(value="q") String searchTerm, Model model) {
        model.addAttribute("results", imbdService.searchShows(searchTerm));
        return "search_results.html";
    }
}
