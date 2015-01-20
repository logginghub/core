package com.logginghub.logging.servers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
import com.logginghub.logging.modules.TimestampFixedRollingFileLogger;
import com.logginghub.logging.modules.TimestampVariableRollingFileLogger;
import com.logginghub.logging.utils.BaseFileLogger;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.Source;
import com.logginghub.utils.Stream;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;
import com.logginghub.utils.module.Module;

public class TestLogsPlayNicelyInTheSameFolder {

    @Test public void test_logs_play_nicely_in_the_same_folder() {

//        Logger.setLevel(TimestampVariableRollingFileLogger.class, Logger.finest);

        DefaultLogEvent eventA1 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(1)
                                                 .toLogEvent();
        DefaultLogEvent eventA2 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(2)
                                                 .toLogEvent();
        DefaultLogEvent eventA3 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(3)
                                                 .toLogEvent();
        DefaultLogEvent eventA4 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(4)
                                                 .toLogEvent();
        DefaultLogEvent eventA5 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(5)
                                                 .toLogEvent();
        DefaultLogEvent eventA6 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(6)
                                                 .toLogEvent();
        DefaultLogEvent eventA7 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(7)
                                                 .toLogEvent();

        DefaultLogEvent eventB1 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(1)
                                                 .toLogEvent();
        DefaultLogEvent eventB2 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(2)
                                                 .toLogEvent();
        DefaultLogEvent eventB3 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(3)
                                                 .toLogEvent();
        DefaultLogEvent eventB4 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(4)
                                                 .toLogEvent();
        DefaultLogEvent eventB5 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(5)
                                                 .toLogEvent();
        DefaultLogEvent eventB6 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(6)
                                                 .toLogEvent();
        DefaultLogEvent eventB7 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(7)
                                                 .toLogEvent();

        // Format the events so we can see how long they will be
        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
        String eventAFormatted = formatter.format(eventA1);

        File folder = FileUtils.createRandomTestFolderForClass(TestLogsPlayNicelyInTheSameFolder.class);
        FileUtils.deleteContents(folder);

        ConfigurableServiceDiscovery configurableDiscovery = new ConfigurableServiceDiscovery();
        Stream<LogEvent> source = new Stream<LogEvent>();
        configurableDiscovery.bind(Source.class, LogEvent.class, source);

        int maximumEventSize = eventAFormatted.getBytes().length;

        FileUtilsWriter writer = FileUtilsWriter.createTestFile(TestLogsPlayNicelyInTheSameFolder.class);
        writer.appendLine("<container>");
        writer.appendLine("<rollingFileLogger folder='{}' maximumFileSize='{}' />", folder.getAbsolutePath(), maximumEventSize);
        writer.appendLine("<timestampVariableRollingFileLogger folder='{}' maximumFileSize='{}'/>", folder.getAbsolutePath(), maximumEventSize);
        writer.appendLine("<timestampFixedRollingFileLogger filename=\"hub.fixed\" folder='{}' maximumFileSize='{}'/>",
                          folder.getAbsolutePath(),
                          maximumEventSize);
        writer.appendLine("</container>");
        writer.close();

        LoggingContainer container = LoggingContainer.fromFile(writer.getFile(), configurableDiscovery);
        container.start();

        FixedTimeProvider time = new FixedTimeProvider(0);
        setTimeProviders(container, time);

        String eventBFormatted = formatter.format(eventB1);

        // Send the first event in
        source.send(eventA1);
        flushAll(container);

        Map<String, File> files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(4));
        assertThat(FileUtils.readAsStringArray(files.get("hub.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(files.get("hub.fixed.log.timedata").length(), is(8L));

        // First roll
        time.setTime(TimeUtils.seconds(1));
        source.send(eventA2);
        flushAll(container);

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(7));
        assertThat(FileUtils.readAsStringArray(files.get("hub.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000001.log"))[0], is(formatter.format(eventA2)));

        assertThat(FileUtils.readAsStringArray(files.get("hub.log.1"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.1970_01_01_000000.0.log"))[0], is(formatter.format(eventA1)));

        // Second roll
        time.setTime(TimeUtils.seconds(2));
        source.send(eventA3);
        flushAll(container);

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));
        assertThat(FileUtils.readAsStringArray(files.get("hub.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000002.log"))[0], is(formatter.format(eventA3)));

        assertThat(FileUtils.readAsStringArray(files.get("hub.log.1"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000001.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.1970_01_01_000001.0.log"))[0], is(formatter.format(eventA2)));

        assertThat(FileUtils.readAsStringArray(files.get("hub.log.2"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.1970_01_01_000000.0.log"))[0], is(formatter.format(eventA1)));

        // Third roll - first zip
        time.setTime(TimeUtils.seconds(3));
        source.send(eventA4);
        flushAll(container);

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(13));

        assertThat(FileUtils.readAsStringArray(files.get("hub.log"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.log"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000003.log"))[0], is(formatter.format(eventA4)));

        assertThat(FileUtils.readAsStringArray(files.get("hub.log.1"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.1970_01_01_000002.0.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000002.log"))[0], is(formatter.format(eventA3)));

        assertThat(FileUtils.readAsStringArray(files.get("hub.log.2"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.1970_01_01_000001.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("hub.fixed.1970_01_01_000001.0.log"))[0], is(formatter.format(eventA2)));

        assertThat(FileUtils.readZipAsStringArray(files.get("hub.log.3.zip"), "hub.log.2")[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readZipAsStringArray(files.get("hub.1970_01_01_000000.log.zip"), "hub.1970_01_01_000000.log")[0],
                   is(formatter.format(eventA1)));
        assertThat(FileUtils.readZipAsStringArray(files.get("hub.fixed.1970_01_01_000000.0.log.zip"), "hub.fixed.1970_01_01_000000.0.log")[0],
                   is(formatter.format(eventA1)));

   

        container.stop();
    }

    private void setTimeProviders(LoggingContainer container, TimeProvider timeProvider) {
        List<Module<?>> modules = container.getModules();
        for (Module<?> module : modules) {
            if (module instanceof BaseFileLogger) {
                BaseFileLogger baseFileLogger = (BaseFileLogger) module;

                if (baseFileLogger instanceof TimestampVariableRollingFileLogger) {
                    TimestampVariableRollingFileLogger logger = (TimestampVariableRollingFileLogger) baseFileLogger;
                    logger.setTimeProvider(timeProvider);
                }
                else if (baseFileLogger instanceof TimestampFixedRollingFileLogger) {
                    TimestampFixedRollingFileLogger logger = (TimestampFixedRollingFileLogger) baseFileLogger;
                    logger.setTimeProvider(timeProvider);
                }

            }
        }
    }

    private void flushAll(LoggingContainer container) {
        List<Module<?>> modules = container.getModules();
        for (Module<?> module : modules) {
            if (module instanceof BaseFileLogger) {
                BaseFileLogger baseFileLogger = (BaseFileLogger) module;
                baseFileLogger.flush();

            }
        }
    }

}
