package org.aria.imdbgraph.scrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Class responsible for scheduling a daily job to update information in the
 * database. This is because IMDB updates their files daily.
 */
@Service
@EnableScheduling
public class DailyUpdateScheduler {

    private final DatabaseUpdater databaseUpdater;

    @Autowired
    DailyUpdateScheduler(DatabaseUpdater databaseUpdater) {
        this.databaseUpdater = databaseUpdater;
    }

    /**
     * The method that is scheduled to run. The cron expression is currently set
     * to run everyday at 8:00 AM UTC.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void launchJob() {
        databaseUpdater.loadAllFiles();
    }
}
