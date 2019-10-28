package org.aria.imdbgraph.scrapper;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class DailyUpdateScheduler {

    @Scheduled(cron = "0 0 8 * * ?")
    public void launchJob() {
    }
}
