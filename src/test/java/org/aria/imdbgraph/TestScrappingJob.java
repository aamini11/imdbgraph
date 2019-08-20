package org.aria.imdbgraph;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("dev")
public class TestScrappingJob {

    @Autowired
    private JobLauncherTestUtils launcher;

    @Test
    public void launchJob() throws Exception {
        JobExecution e = launcher.launchJob();
        Assert.assertEquals(ExitStatus.COMPLETED, e.getExitStatus());
    }
}
