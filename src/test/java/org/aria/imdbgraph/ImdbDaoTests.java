package org.aria.imdbgraph;

import org.aria.imdbgraph.imdb.ImdbDao;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@TestConfiguration
public class ImdbDaoTests {

    @Autowired
    private ImdbDao imdbDao;


}
