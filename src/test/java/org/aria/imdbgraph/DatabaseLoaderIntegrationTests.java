package org.aria.imdbgraph;

import org.aria.imdbgraph.scrapper.DatabaseUpdater;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class DatabaseLoaderIntegrationTests {

    @Autowired
    private DatabaseUpdater databaseUpdater;

    @Test
    public void testRun() {
        databaseUpdater.loadAllFiles();
    }

}
