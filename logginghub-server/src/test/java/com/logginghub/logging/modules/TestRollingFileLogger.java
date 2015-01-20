package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
import com.logginghub.logging.logeventformatters.log4j.Log4jPatternLogEventFormatter;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.modules.RollingFileLogger;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.Source;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.module.ServiceDiscovery;

public class TestRollingFileLogger {

    @Test public void test_two_logs_in_same_folder() {

        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        RollingFileLoggerConfiguration configurationA = new RollingFileLoggerConfiguration();
        RollingFileLoggerConfiguration configurationB = new RollingFileLoggerConfiguration();

        configurationA.setFolder(folder.getAbsolutePath());
        configurationB.setFolder(folder.getAbsolutePath());

        configurationA.setFilename("logA");
        configurationB.setFilename("logB");

        configurationA.setChannels("channel/a");
        configurationB.setChannels("channel/b");

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
        String eventBFormatted = formatter.format(eventB1);

        // Set the file sizes so we'll get one line per file before rolling
        configurationA.setMaximumFileSize(eventAFormatted.getBytes().length);
        configurationB.setMaximumFileSize(eventBFormatted.getBytes().length);

        // Create the mock service discovery
        ServiceDiscovery discovery = Mockito.mock(ServiceDiscovery.class);
        Multiplexer<LogEvent> source = new Multiplexer<LogEvent>();
        Mockito.when(discovery.findService(Mockito.eq(Source.class), Mockito.eq(LogEvent.class), Mockito.anyString())).thenReturn(source);

        // Create the two loggers
        RollingFileLogger loggerA = new RollingFileLogger();
        RollingFileLogger loggerB = new RollingFileLogger();

        loggerA.configure(configurationA, discovery);
        loggerB.configure(configurationB, discovery);

        loggerA.start();
        loggerB.start();

        // Send the first event in
        source.send(eventA1);
        loggerA.flush();

        assertThat(folder.listFiles().length, is(1));
        assertThat(FileUtils.readAsStringArray(folder.listFiles()[0])[0], is(formatter.format(eventA1)));

        // And the second
        source.send(eventB1);
        loggerB.flush();

        Map<String, File> files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(2));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB1)));

        // First roll
        source.send(eventA2);
        source.send(eventB2);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(4));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.1"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.1"))[0], is(formatter.format(eventB1)));

        // Second roll
        source.send(eventA3);
        source.send(eventB3);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(6));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.1"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.1"))[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.2"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.2"))[0], is(formatter.format(eventB1)));

        // First zip roll
        source.send(eventA4);
        source.send(eventB4);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(8));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.1"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.1"))[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.2"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.2"))[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.3.zip"), "logA.log.2")[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.3.zip"), "logB.log.2")[0], is(formatter.format(eventB1)));

        // Second zip roll
        source.send(eventA5);
        source.send(eventB5);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA5)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB5)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.1"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.1"))[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.2"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.2"))[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.3.zip"), "logA.log.2")[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.3.zip"), "logB.log.2")[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.4.zip"), "logA.log.2")[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.4.zip"), "logB.log.2")[0], is(formatter.format(eventB1)));

        // First delete
        source.send(eventA6);
        source.send(eventB6);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA6)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB6)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.1"))[0], is(formatter.format(eventA5)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.1"))[0], is(formatter.format(eventB5)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.2"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.2"))[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.3.zip"), "logA.log.2")[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.3.zip"), "logB.log.2")[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.4.zip"), "logA.log.2")[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.4.zip"), "logB.log.2")[0], is(formatter.format(eventB2)));

        // Secoond delete
        source.send(eventA7);
        source.send(eventB7);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log"))[0], is(formatter.format(eventA7)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log"))[0], is(formatter.format(eventB7)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.1"))[0], is(formatter.format(eventA6)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.1"))[0], is(formatter.format(eventB6)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.log.2"))[0], is(formatter.format(eventA5)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.log.2"))[0], is(formatter.format(eventB5)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.3.zip"), "logA.log.2")[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.3.zip"), "logB.log.2")[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.log.4.zip"), "logA.log.2")[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.log.4.zip"), "logB.log.2")[0], is(formatter.format(eventB3)));

        loggerA.close();
        loggerB.close();
    }

    @Test public void test_multiple_channels() {
        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        RollingFileLoggerConfiguration configurationA = new RollingFileLoggerConfiguration();

        configurationA.setFolder(folder.getAbsolutePath());

        configurationA.setFilename("logA.log");

        configurationA.setChannels("    channel/a   ,     channel/b    ");

        DefaultLogEvent eventA1 = LogEventBuilder.start()
                                                 .setMessage("This is a channel A message")
                                                 .setChannel("channel/a")
                                                 .setLocalCreationTimeMillis(1)
                                                 .toLogEvent();

        DefaultLogEvent eventB1 = LogEventBuilder.start()
                                                 .setMessage("This is a channel B message")
                                                 .setChannel("channel/b")
                                                 .setLocalCreationTimeMillis(1)
                                                 .toLogEvent();

        // Create the mock service discovery
        ServiceDiscovery discovery = Mockito.mock(ServiceDiscovery.class);
        Multiplexer<LogEvent> source = new Multiplexer<LogEvent>();
        Mockito.when(discovery.findService(Mockito.eq(Source.class), Mockito.eq(LogEvent.class), Mockito.anyString())).thenReturn(source);

        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();

        // Create and configure
        RollingFileLogger loggerA = new RollingFileLogger();
        loggerA.configure(configurationA, discovery);
        loggerA.start();

        // Send the first event in
        source.send(eventA1);
        loggerA.flush();

        assertThat(folder.listFiles().length, is(1));
        assertThat(FileUtils.readAsStringArray(folder.listFiles()[0])[0], is(formatter.format(eventA1)));

        // And the seconds
        source.send(eventB1);
        loggerA.flush();

        assertThat(folder.listFiles().length, is(1));
        assertThat(FileUtils.readAsStringArray(folder.listFiles()[0])[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(folder.listFiles()[0])[1], is(formatter.format(eventB1)));

        loggerA.close();
    }

    @Test public void test_channels_custom_sub() {

        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        RollingFileLoggerConfiguration configuration = new RollingFileLoggerConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        RollingFileLogger logger = new RollingFileLogger();
        ServiceDiscovery discovery = Mockito.mock(ServiceDiscovery.class);
        Source<LogEvent> source = new Multiplexer<LogEvent>();
        Mockito.when(discovery.findService(Mockito.eq(Source.class), Mockito.eq(LogEvent.class), Mockito.anyString())).thenReturn(source);

        configuration.setChannels("custom/sub");
        logger.configure(configuration, discovery);

        DefaultLogEvent defaultEvent = LogEventBuilder.start().setMessage("default message").toLogEvent();
        DefaultLogEvent eventsEvent = LogEventBuilder.start().setMessage("events message").setChannel("events").toLogEvent();
        DefaultLogEvent customEvent = LogEventBuilder.start().setMessage("custom message").setChannel("custom").toLogEvent();
        DefaultLogEvent customSubEvent = LogEventBuilder.start().setMessage("custom sub message").setChannel("custom/sub").toLogEvent();

        logger.send(defaultEvent);
        logger.send(eventsEvent);
        logger.send(customEvent);
        logger.send(customSubEvent);

        logger.close();

        File[] list = folder.listFiles();
        assertThat(list.length, is(1));

        String[] content = FileUtils.readAsStringArray(list[0]);
        assertThat(content.length, is(1));
        assertThat(content[0], containsString("custom sub message"));
    }

    @Test public void test_channels_custom() {

        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        RollingFileLoggerConfiguration configuration = new RollingFileLoggerConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        RollingFileLogger logger = new RollingFileLogger();
        ServiceDiscovery discovery = Mockito.mock(ServiceDiscovery.class);
        Source<LogEvent> source = new Multiplexer<LogEvent>();
        Mockito.when(discovery.findService(Mockito.eq(Source.class), Mockito.eq(LogEvent.class), Mockito.anyString())).thenReturn(source);

        configuration.setChannels("custom");
        logger.configure(configuration, discovery);

        DefaultLogEvent defaultEvent = LogEventBuilder.start().setMessage("default message").toLogEvent();
        DefaultLogEvent eventsEvent = LogEventBuilder.start().setMessage("events message").setChannel("events").toLogEvent();
        DefaultLogEvent customEvent = LogEventBuilder.start().setMessage("custom message").setChannel("custom").toLogEvent();
        DefaultLogEvent customSubEvent = LogEventBuilder.start().setMessage("custom sub message").setChannel("custom/sub").toLogEvent();

        logger.send(defaultEvent);
        logger.send(eventsEvent);
        logger.send(customEvent);
        logger.send(customSubEvent);

        logger.close();

        File[] list = folder.listFiles();
        assertThat(list.length, is(1));

        String[] content = FileUtils.readAsStringArray(list[0]);
        assertThat(content.length, is(2));
        assertThat(content[0], containsString("custom message"));
        assertThat(content[1], containsString("custom sub message"));
    }

    @Test public void test_folder() throws LoggingMessageSenderException {

        File folder = FileUtils.createRandomTestFileForClass(TestRollingFileLogger.class);

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setFolder(folder.getAbsolutePath());
        logger.setOpenWithAppend(false);
        logger.setAutoNewline(false);
        logger.setFormatter(new Log4jPatternLogEventFormatter("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n"));

        String loggingFilename = "log.txt";

        File expectedFile = new File(folder, loggingFilename);
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        logger.send(message);
        logger.close();

        String content = FileUtils.readAsString(expectedFile);
        String expected = StringUtils.format("[{}] test {} {} INFO   getLogRecord1  - This is mock record 1{}",
                                             Thread.currentThread().getName(),
                                             NetUtils.getLocalHostname(),
                                             NetUtils.getLocalIP(),
                                             StringUtils.newline);
        assertThat(content, is(endsWith(expected)));
    }

    @Test public void test_log4j_pattern() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setOpenWithAppend(false);
        logger.setAutoNewline(false);
        logger.setFormatter(new Log4jPatternLogEventFormatter("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n"));

        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";

        File expectedFile = new File(loggingFilename);
        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        logger.send(message);
        logger.close();

        String content = FileUtils.readAsString(expectedFile);
        String expected = StringUtils.format("[{}] test {} {} INFO   getLogRecord1  - This is mock record 1{}",
                                             Thread.currentThread().getName(),
                                             NetUtils.getLocalHostname(),
                                             NetUtils.getLocalIP(),
                                             StringUtils.newline);
        assertThat(content, is(endsWith(expected)));
    }

    @Test public void test_flushing_off() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setOpenWithAppend(false);
        logger.setNumberOfFiles(8);
        logger.setNumberOfFilesCompressed(17);

        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";

        File expectedFile = new File(loggingFilename);
        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        // Write the first entry
        assertThat(expectedFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));

        String content = FileUtils.readAsString(expectedFile);
        assertThat(content.length(), is(0));

        logger.close();

        content = FileUtils.readAsString(expectedFile);

        if (OSUtils.isWindows()) {
            assertThat(content.length(), is(164));
        }
        else {
            assertThat(content.length(), is(163));
        }
    }

    @Test public void test_flushing_on() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setOpenWithAppend(false);
        logger.setNumberOfFiles(8);
        logger.setForceFlush(true);
        logger.setNumberOfFilesCompressed(17);

        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";

        File expectedFile = new File(loggingFilename);
        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        // Write the first entry
        assertThat(expectedFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));

        String content = FileUtils.readAsString(expectedFile);
        if (OSUtils.isWindows()) {
            assertThat(content.length(), is(164));
        }
        else {
            assertThat(content.length(), is(163));
        }

        logger.close();

        content = FileUtils.readAsString(expectedFile);
        if (OSUtils.isWindows()) {
            assertThat(content.length(), is(164));
        }
        else {
            assertThat(content.length(), is(163));
        }
    }

    @Test public void testFileNumberPadding() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setOpenWithAppend(false);
        logger.setNumberOfFiles(8);
        logger.setNumberOfFilesCompressed(17);

        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        String firstRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.01";
        File expectedFile = new File(loggingFilename);
        File expectedFirstRollFile = new File(firstRollFilename);

        // Write the first entry
        assertThat(expectedFile.exists(), is(false));
        assertThat(expectedFirstRollFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));

        logger.close();

        // Write another entry, this should cause a roll
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));

        logger.close();
    }

    @Before public void cleardown() {
        String loggingPath = "target/temp/aggregatedFileLoggerTest";
        File loggingFolder = new File(loggingPath);
        if (loggingFolder.exists()) {
            FileUtils.deleteContents(loggingFolder);
            assertThat(loggingFolder.listFiles(), is(new File[] {}));
        }
    }

    @Test public void testOpenWithAppend() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setOpenWithAppend(true);
        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        String firstRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.1";
        File expectedFile = new File(loggingFilename);
        File expectedFirstRollFile = new File(firstRollFilename);
        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        // Write the first entry
        assertThat(expectedFile.exists(), is(false));
        assertThat(expectedFirstRollFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));

        logger.close();

        // Write another entry, this should go into the same file
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));

        logger.close();
    }

    @Test public void testOpenWithoutAppend() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setOpenWithAppend(false);
        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        String firstRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.1";
        File expectedFile = new File(loggingFilename);
        File expectedFirstRollFile = new File(firstRollFilename);

        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        // Write the first entry
        assertThat(expectedFile.exists(), is(false));
        assertThat(expectedFirstRollFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));

        logger.close();

        // Write another entry, this should cause a roll
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));

        logger.close();
    }

    @Test public void testWritingSingleFile() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        File expectedFile = new File(loggingFilename);
        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        assertThat(expectedFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));

        logger.close();
    }

    @Test public void testRollover() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        String firstRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.1";

        File expectedFile = new File(loggingFilename);
        File expectedFirstRollFile = new File(firstRollFilename);

        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        logger.setMaximumFileSize(100);

        assertThat(expectedFile.exists(), is(false));
        logger.send(message);
        assertThat(expectedFile.exists(), is(true));

        // This should cause it to roll over
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));

        logger.close();
    }

    @Test public void testFullRollover() throws LoggingMessageSenderException {

        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test");
        LogEventMessage message = new LogEventMessage(logEvent);

        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
        String formatted = formatter.format(logEvent);

        RollingFileLogger logger = new RollingFileLogger();
        logger.setNumberOfFiles(3);
        logger.setNumberOfFilesCompressed(3);

        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        String firstRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.1";
        String secondRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.2";
        String thirdRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.3";

        String firstZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.3.zip";
        String secondZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.4.zip";
        String thirdZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.5.zip";
        String fourthZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.6.zip";

        File expectedFile = new File(loggingFilename);

        File expectedFirstRollFile = new File(firstRollFilename);
        File expectedSecondRollFile = new File(secondRollFilename);
        File expectedThirdRollFile = new File(thirdRollFilename);

        File expectedFirstZipFile = new File(firstZipFilename);
        File expectedSecondZipFile = new File(secondZipFilename);
        File expectedThirdZipFile = new File(thirdZipFilename);
        File expectedFourthZipFile = new File(fourthZipFilename);

        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        logger.setMaximumFileSize(formatted.length() + 1);

        // Nothing logged
        assertThat(expectedFile.exists(), is(false));
        assertThat(expectedFirstRollFile.exists(), is(false));
        assertThat(expectedSecondRollFile.exists(), is(false));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        logger.send(message);

        // First line in the first file
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));
        assertThat(expectedSecondRollFile.exists(), is(false));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // First roll
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(false));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // Second roll
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // First zip roll, make sure we didn't roll into the third file
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // Second zip roll
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        logger.send(message);

        // Third zip roll
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(true));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // First deletion, make sure we didn't roll into the fourth zip file
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(true));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // Second deletion, business as usual from here on in
        logger.send(message);

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(true));
        assertThat(expectedFourthZipFile.exists(), is(false));

        logger.close();
    }

    @Test public void test_full_rollover_and_file_contents() throws LoggingMessageSenderException {

        int messageCounter = 0;

        RollingFileLogger logger = new RollingFileLogger();
        logger.setNumberOfFiles(3);
        logger.setNumberOfFilesCompressed(3);

        String loggingFilename = "target/temp/aggregatedFileLoggerTest/log.txt";
        String firstRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.1";
        String secondRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.2";
        String thirdRollFilename = "target/temp/aggregatedFileLoggerTest/log.txt.3";

        String firstZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.3.zip";
        String secondZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.4.zip";
        String thirdZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.5.zip";
        String fourthZipFilename = "target/temp/aggregatedFileLoggerTest/log.txt.6.zip";

        File expectedFile = new File(loggingFilename);

        File expectedFirstRollFile = new File(firstRollFilename);
        File expectedSecondRollFile = new File(secondRollFilename);
        File expectedThirdRollFile = new File(thirdRollFilename);

        File expectedFirstZipFile = new File(firstZipFilename);
        File expectedSecondZipFile = new File(secondZipFilename);
        File expectedThirdZipFile = new File(thirdZipFilename);
        File expectedFourthZipFile = new File(fourthZipFilename);

        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
        String formatted = formatter.format(generateMessage(1).getLogEvent());

        logger.setFolder("target/temp/aggregatedFileLoggerTest");
        logger.setFileName("log");
        logger.setFileExtension(".txt");

        logger.setMaximumFileSize(formatted.getBytes().length + 1);

        // Nothing logged
        assertThat(expectedFile.exists(), is(false));
        assertThat(expectedFirstRollFile.exists(), is(false));
        assertThat(expectedSecondRollFile.exists(), is(false));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        logger.send(generateMessage(messageCounter++));

        // First line in the first file
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(false));
        assertThat(expectedSecondRollFile.exists(), is(false));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        // First roll
        logger.send(generateMessage(messageCounter++));

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(false));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 0);

        // Second roll
        logger.send(generateMessage(messageCounter++));

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(false));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 1);
        assertFileContents(secondRollFilename, 0);

        // First zip roll, make sure we didn't roll into the third file
        logger.send(generateMessage(messageCounter++));

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(false));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 2);
        assertFileContents(secondRollFilename, 1);
        assertZipFileContents(firstZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 0);

        // Second zip roll
        logger.send(generateMessage(messageCounter++));

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(false));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 3);
        assertFileContents(secondRollFilename, 2);
        assertZipFileContents(firstZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 1);
        assertZipFileContents(secondZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 0);

        logger.send(generateMessage(messageCounter++));

        // Third zip roll
        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(true));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 4);
        assertFileContents(secondRollFilename, 3);
        assertZipFileContents(firstZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 2);
        assertZipFileContents(secondZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 1);
        assertZipFileContents(thirdZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 0);

        // First deletion, make sure we didn't roll into the fourth zip file
        logger.send(generateMessage(messageCounter++));

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(true));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 5);
        assertFileContents(secondRollFilename, 4);
        assertZipFileContents(firstZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 3);
        assertZipFileContents(secondZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 2);
        assertZipFileContents(thirdZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 1);

        // Second deletion, business as usual from here on in
        logger.send(generateMessage(messageCounter++));

        assertThat(expectedFile.exists(), is(true));
        assertThat(expectedFirstRollFile.exists(), is(true));
        assertThat(expectedSecondRollFile.exists(), is(true));
        assertThat(expectedThirdRollFile.exists(), is(false));
        assertThat(expectedFirstZipFile.exists(), is(true));
        assertThat(expectedSecondZipFile.exists(), is(true));
        assertThat(expectedThirdZipFile.exists(), is(true));
        assertThat(expectedFourthZipFile.exists(), is(false));

        assertFileContents(firstRollFilename, 6);
        assertFileContents(secondRollFilename, 5);
        assertZipFileContents(firstZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 4);
        assertZipFileContents(secondZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 3);
        assertZipFileContents(thirdZipFilename, StringUtils.afterLast(secondRollFilename, "/"), 2);

        logger.close();
    }

    private void assertZipFileContents(String zipFilename, String internalFilename, int... messageNumbers) {
        String[] splitIntoLines = StringUtils.splitIntoLines(FileUtils.readFromZipAsString(new File(zipFilename), internalFilename));
        assertFileContents(splitIntoLines, messageNumbers);
    }

    private void assertFileContents(String filename, int... messageNumbers) {
        String[] lines = FileUtils.readAsStringArray(filename);
        assertFileContents(lines, messageNumbers);
    }

    private void assertFileContents(String[] lines, int... messageNumbers) {
        assertThat("File doesnt contain the expected number of lines", lines.length, is(messageNumbers.length));
        for (int i = 0; i < lines.length; i++) {
            assertThat(lines[i], endsWith(Integer.toString(messageNumbers[i])));
        }
    }

    private LogEventMessage generateMessage(int i) {
        DefaultLogEvent logEvent = LogEventFactory.createFullLogEvent1("test message");
        logEvent.setMessage("Message " + i);
        LogEventMessage message = new LogEventMessage(logEvent);
        return message;
    }

    @Test public void test_send_protection_against_closed_stream() throws LoggingMessageSenderException {
        File tempFolder = FileUtils.createRandomTestFolderForClass(this.getClass());
        System.out.println(tempFolder.getAbsolutePath());
        RollingFileLogger logger = new RollingFileLogger();
        logger.setFolder(tempFolder);

        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        LogEventMessage message = new LogEventMessage(event);

        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
        String formattedEvent = formatter.format(event);
        String formattedEventWithNewline = formattedEvent + StringUtils.newline;
        long singleItemSize = formatter.format(event).getBytes().length;
        long singleItemSizeWithNewline = singleItemSize + StringUtils.newline.getBytes().length;

        logger.send(message);
        logger.hackClose();
        logger.send(message);
        logger.close();

        File expected = new File(tempFolder, "log.txt");
        assertThat(expected.exists(), is(true));
        assertThat(expected.length(), is(singleItemSizeWithNewline * 2));
        assertThat(FileUtils.read(expected), is(formattedEventWithNewline + formattedEventWithNewline));
    }
}
