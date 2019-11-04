package org.aria.imdbgraph.scrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class DailyUpdateScheduler {

    private final DatabaseUpdater databaseUpdater;

    @Autowired
    public DailyUpdateScheduler(DatabaseUpdater databaseUpdater) {
        this.databaseUpdater = databaseUpdater;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void launchJob() {
        databaseUpdater.loadAllFiles();
    }
}
