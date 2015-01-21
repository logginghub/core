package com.logginghub.integrationtests.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.repository.BinaryLogFileReader;
import com.logginghub.logging.repository.LocalDiskRepository;
import com.logginghub.logging.repository.LogDataProcessor;
import com.logginghub.logging.repository.config.LocalDiskRepositoryConfiguration;
import com.logginghub.utils.TimerUtils;

@Ignore // takes too long
public class LocalDiskRepositoryTest {

    @Test public void testWritingLargeData() {

        LocalDiskRepositoryConfiguration configuration = new LocalDiskRepositoryConfiguration();
        configuration.setDataFolder("test-data");
        configuration.setFileDurationMilliseconds(10000000);
        configuration.setHubConnectionString("");
        configuration.setOverrideEventTime(false);

        final LocalDiskRepository repo = new LocalDiskRepository(configuration);
        repo.setBufferSize(50 * 1024 * 1024);
        repo.startStatsTimer();

        final DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1();
        logEvent.setLocalCreationTimeMillis(50);

        TimerUtils.repeatFor(5, TimeUnit.SECONDS, new Runnable() {
            public void run() {
                repo.process(logEvent);
            }
        });

        File currentFile = repo.close();

        BinaryLogFileReader reader = new BinaryLogFileReader();
        reader.readFile(currentFile, currentFile, new LogDataProcessor() {
            public void onNewLogEvent(LogEvent event) {}

            public void processingStarted(File resultsFolder) {}

            public void processingEnded() {}
        });

        currentFile.delete();
    }

    @Test public void testOverwriteEventTime() {

        LocalDiskRepositoryConfiguration configuration = new LocalDiskRepositoryConfiguration();
        configuration.setDataFolder("test-data");
        configuration.setFileDurationMilliseconds(1000);
        configuration.setHubConnectionString("");
        configuration.setOverrideEventTime(true);

        LocalDiskRepository repo = new LocalDiskRepository(configuration);

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1();
        logEvent.setLocalCreationTimeMillis(50);

        long roughTime = System.currentTimeMillis();
        repo.process(logEvent);
        File currentFile = repo.close();

        BinaryLogFileReader reader = new BinaryLogFileReader();
        List<LogEvent> events = reader.readAll(currentFile);

        try {
            assertThat(events.size(), is(1));
            assertThat(events.get(0).getOriginTime(), is(not(50L)));
            assertThat((double) events.get(0).getOriginTime(), is(closeTo(roughTime, 1000)));
        }
        finally {
            currentFile.delete();
        }
    }

    @Test public void testNaturalEventTime() {

        LocalDiskRepositoryConfiguration configuration = new LocalDiskRepositoryConfiguration();
        configuration.setDataFolder("test-data");
        configuration.setFileDurationMilliseconds(1000);
        configuration.setHubConnectionString("");
        configuration.setOverrideEventTime(false);

        LocalDiskRepository repo = new LocalDiskRepository(configuration);

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1();
        logEvent.setLocalCreationTimeMillis(50);

        repo.process(logEvent);
        File currentFile = repo.close();

        BinaryLogFileReader reader = new BinaryLogFileReader();
        List<LogEvent> events = reader.readAll(currentFile);

        try {
            assertThat(events.size(), is(1));
            assertThat(events.get(0).getOriginTime(), is(50L));
        }
        finally {
            currentFile.delete();
        }
    }
}
