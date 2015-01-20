package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
import com.logginghub.logging.logeventformatters.log4j.Log4jPatternLogEventFormatter;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.modules.TimestampVariableRollingFileLogger;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Pointer;
import com.logginghub.utils.Source;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.module.ServiceDiscovery;

public class TestTimestampVariableRollingFileLogger {

    private long singleItemSize;
    private long singleItemSizeWithNewline;

    private Pointer<Long> timePointer;
    private TimestampVariableRollingFileLogger logger = new TimestampVariableRollingFileLogger();
    private File tempFolder;
    private DefaultLogEvent event;
    private LogEventMessage message;
    private String formattedEventWithNewline;
    private String formattedEvent;

    @Before public void setup() {
        timePointer = new Pointer<Long>(0L);
        logger.setTimeProvider(new TimeProvider() {
            @Override public long getTime() {
                return timePointer.value;
            }
        });

        timePointer.value = 0L;

        tempFolder = FileUtils.createRandomTestFolderForClass(this.getClass());

        logger.setFolder(tempFolder);
        logger.setFileName("log");
        logger.setFileExtension(".txt");
        logger.setTimeFormat("yyyy_MM_dd_HHmmss");

        event = LogEventFactory.createFullLogEvent1();
        message = new LogEventMessage(event);

        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
        formattedEvent = formatter.format(event);
        formattedEventWithNewline = formattedEvent + StringUtils.newline;
        singleItemSize = formatter.format(event).getBytes().length;
        singleItemSizeWithNewline = singleItemSize + StringUtils.newline.getBytes().length;
    }

    @Test public void test_channels_custom_sub() {

        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        TimestampVariableRollingFileLoggerConfiguration configuration = new TimestampVariableRollingFileLoggerConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        TimestampVariableRollingFileLogger logger = new TimestampVariableRollingFileLogger();
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
        System.out.println(Arrays.toString(list));
        assertThat(list.length, is(1));

        String[] content = FileUtils.readAsStringArray(list[0]);
        assertThat(content.length, is(1));
        assertThat(content[0], containsString("custom sub message"));
    }

    @Test public void test_channels_custom() {

        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        TimestampVariableRollingFileLoggerConfiguration configuration = new TimestampVariableRollingFileLoggerConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        TimestampVariableRollingFileLogger logger = new TimestampVariableRollingFileLogger();
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
        System.out.println(Arrays.toString(list));
        assertThat(list.length, is(1));

        String[] content = FileUtils.readAsStringArray(list[0]);
        assertThat(content.length, is(2));
        assertThat(content[0], containsString("custom message"));
        assertThat(content[1], containsString("custom sub message"));
    }

    @Test public void test_log4j_pattern() throws LoggingMessageSenderException {

        logger.setOpenWithAppend(false);
        logger.setAutoNewline(false);
        logger.setFormatter(new Log4jPatternLogEventFormatter("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n"));

        logger.send(message);
        logger.close();

        File expectedFile = new File(tempFolder, "log.1970_01_01_000000.txt");
        String content = FileUtils.readAsString(expectedFile);
        String expected = StringUtils.format("[{}] TestApplication {} {} INFO   getLogRecord1  - This is mock record 1{}",
                                             Thread.currentThread().getName(),
                                             NetUtils.getLocalHostname(),
                                             NetUtils.getLocalIP(),
                                             StringUtils.newline);
        assertThat(content, is(endsWith(expected)));
    }

    @Test public void test_flush_off() throws LoggingMessageSenderException {

        File expected = new File(tempFolder, "log.1970_01_01_000000.txt");
        assertThat(expected.exists(), is(false));

        logger.send(message);

        assertThat(expected.exists(), is(true));
        String content = FileUtils.readAsString(expected);
        assertThat(content.length(), is(0));

        logger.close();

        assertThat(expected.length(), is(singleItemSizeWithNewline));
        content = FileUtils.readAsString(expected);
        assertThat((long) content.length(), is(singleItemSizeWithNewline));
    }

    @Test public void test_flush_on() throws LoggingMessageSenderException {

        logger.setForceFlush(true);

        File expected = new File(tempFolder, "log.1970_01_01_000000.txt");
        assertThat(expected.exists(), is(false));

        logger.send(message);

        assertThat(expected.exists(), is(true));
        String content = FileUtils.readAsString(expected);
        assertThat((long) content.length(), is(singleItemSizeWithNewline));

        logger.close();

        assertThat(expected.length(), is(singleItemSizeWithNewline));
        content = FileUtils.readAsString(expected);
        assertThat((long) content.length(), is(singleItemSizeWithNewline));
    }

    @Test public void test_send() throws LoggingMessageSenderException {

        logger.send(message);
        logger.close();

        File expected = new File(tempFolder, "log.1970_01_01_000000.txt");
        assertThat(expected.exists(), is(true));
        assertThat(expected.length(), is(singleItemSizeWithNewline));
    }

    @Test public void test_send_and_roll_same_time() throws LoggingMessageSenderException {

        timePointer.value = 0L;
        // Roll on the third line
        logger.setMaximumFileSize(singleItemSizeWithNewline * 3);

        logger.send(message);
        logger.send(message);
        logger.send(message);

        logger.send(message);

        logger.close();

        File expected1 = new File(tempFolder, "log.1970_01_01_000000.txt");
        File expected2 = new File(tempFolder, "log.1970_01_01_000000.1.txt");
        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(true));
        assertThat(expected1.length(), is(singleItemSizeWithNewline * 3));
        assertThat(expected2.length(), is(singleItemSizeWithNewline));
    }

    @Test public void test_send_protection_against_closed_stream() throws LoggingMessageSenderException {

        logger.send(message);
        logger.hackClose();
        logger.send(message);        
        logger.close();

        System.out.println(tempFolder.getAbsolutePath());
        File expected = new File(tempFolder, "log.1970_01_01_000000.txt");
        assertThat(expected.exists(), is(true));
        assertThat(expected.length(), is(singleItemSizeWithNewline * 2));
        assertThat(FileUtils.read(expected), is(formattedEventWithNewline + formattedEventWithNewline));
    }
    
    @Test public void test_send_and_roll_diff_time() throws LoggingMessageSenderException {

        timePointer.value = 0L;
        // Roll on the third line
        logger.setMaximumFileSize(singleItemSizeWithNewline * 3);

        logger.send(message);
        logger.send(message);

        // Move the time ready for the roll
        timePointer.value = TimeUnit.HOURS.toMillis(2);
        logger.send(message);
        logger.send(message);

        logger.close();

        File expected1 = new File(tempFolder, "log.1970_01_01_000000.txt");
        File expected2 = new File(tempFolder, "log.1970_01_01_020000.txt");
        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(true));
        assertThat(expected1.length(), is(singleItemSizeWithNewline * 3));
        assertThat(expected2.length(), is(singleItemSizeWithNewline));
    }

    @Test public void test_send_and_roll_diff_time_first_zip() throws LoggingMessageSenderException {

        timePointer.value = 0L;
        // Roll on the third line
        logger.setMaximumFileSize(singleItemSizeWithNewline * 3);
        logger.setNumberOfFiles(2);

        logger.send(message);
        logger.send(message);

        // Move the time ready for the roll
        timePointer.value = TimeUnit.HOURS.toMillis(2);
        logger.send(message);
        logger.send(message);
        logger.send(message);

        timePointer.value = TimeUnit.HOURS.toMillis(4);
        logger.send(message);
        logger.send(message);
        logger.close();

        String expected1Raw = "log.1970_01_01_000000.txt";
        File expected1 = new File(tempFolder, "log.1970_01_01_000000.txt.zip");
        File expected2 = new File(tempFolder, "log.1970_01_01_020000.txt");
        File expected3 = new File(tempFolder, "log.1970_01_01_040000.txt");
        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(true));
        assertThat(expected3.exists(), is(true));

        assertThat((long) FileUtils.readFromZip(expected1, expected1Raw).length, is(singleItemSizeWithNewline * 3));
        assertThat(expected2.length(), is(singleItemSizeWithNewline * 3));
        assertThat(expected3.length(), is(singleItemSizeWithNewline));
    }

    @Test public void test_send_and_roll_diff_time_delete_oldest_zip() throws LoggingMessageSenderException {

        timePointer.value = 0L;
        // Roll on the third line
        logger.setMaximumFileSize(singleItemSizeWithNewline * 2);

        logger.setNumberOfFiles(2);
        logger.setNumberOfFilesCompressed(2);

        File expected1 = new File(tempFolder, "log.1970_01_01_000000.txt");
        File expected2 = new File(tempFolder, "log.1970_01_01_020000.txt");
        File expected3 = new File(tempFolder, "log.1970_01_01_040000.txt");
        File expected4 = new File(tempFolder, "log.1970_01_01_060000.txt");
        File expected5 = new File(tempFolder, "log.1970_01_01_080000.txt");

        File expected1Zip = new File(tempFolder, "log.1970_01_01_000000.txt.zip");
        File expected2Zip = new File(tempFolder, "log.1970_01_01_020000.txt.zip");
        File expected3Zip = new File(tempFolder, "log.1970_01_01_040000.txt.zip");

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(false));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        // First file
        logger.send(message);

        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(false));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        logger.send(message);
        timePointer.value = TimeUnit.HOURS.toMillis(2);
        logger.send(message);

        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(true));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(false));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        // Second file
        logger.send(message);
        timePointer.value = TimeUnit.HOURS.toMillis(4);
        logger.send(message);

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(true));
        assertThat(expected1Zip.exists(), is(true));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(true));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        // Third file
        logger.send(message);
        timePointer.value = TimeUnit.HOURS.toMillis(6);
        logger.send(message);

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(true));
        assertThat(expected2Zip.exists(), is(true));
        assertThat(expected3.exists(), is(true));
        assertThat(expected4.exists(), is(true));
        assertThat(expected5.exists(), is(false));

        // Fourth file
        logger.send(message);
        timePointer.value = TimeUnit.HOURS.toMillis(8);
        logger.send(message);
        logger.close();

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(true));
        assertThat(expected3Zip.exists(), is(true));
        assertThat(expected4.exists(), is(true));
        assertThat(expected5.exists(), is(true));
    }

    @Test public void test_send_and_roll_same_time_delete_oldest_zip() throws LoggingMessageSenderException {

        timePointer.value = 0L;
        // Roll on the third line
        logger.setMaximumFileSize(singleItemSizeWithNewline * 2);

        logger.setNumberOfFiles(2);
        logger.setNumberOfFilesCompressed(2);

        File expected1 = new File(tempFolder, "log.1970_01_01_000000.txt");
        File expected2 = new File(tempFolder, "log.1970_01_01_000000.1.txt");
        File expected3 = new File(tempFolder, "log.1970_01_01_000000.2.txt");
        File expected4 = new File(tempFolder, "log.1970_01_01_000000.3.txt");
        File expected5 = new File(tempFolder, "log.1970_01_01_000000.4.txt");

        File expected1Zip = new File(tempFolder, "log.1970_01_01_000000.txt.zip");
        File expected2Zip = new File(tempFolder, "log.1970_01_01_000000.1.txt.zip");
        File expected3Zip = new File(tempFolder, "log.1970_01_01_000000.2.txt.zip");

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(false));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        // First file
        logger.send(message);
        logger.send(message);

        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(false));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        logger.send(message);
        logger.send(message);

        assertThat(expected1.exists(), is(true));
        assertThat(expected2.exists(), is(true));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(false));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        // Second file
        logger.send(message);
        logger.send(message);

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(true));
        assertThat(expected1Zip.exists(), is(true));
        assertThat(expected2Zip.exists(), is(false));
        assertThat(expected3.exists(), is(true));
        assertThat(expected4.exists(), is(false));
        assertThat(expected5.exists(), is(false));

        // Third file
        logger.send(message);
        logger.send(message);

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(true));
        assertThat(expected2Zip.exists(), is(true));
        assertThat(expected3.exists(), is(true));
        assertThat(expected4.exists(), is(true));
        assertThat(expected5.exists(), is(false));

        // Fourth file
        logger.send(message);
        logger.send(message);
        logger.close();

        assertThat(expected1.exists(), is(false));
        assertThat(expected2.exists(), is(false));
        assertThat(expected1Zip.exists(), is(false));
        assertThat(expected2Zip.exists(), is(true));
        assertThat(expected3Zip.exists(), is(true));
        assertThat(expected4.exists(), is(true));
        assertThat(expected5.exists(), is(true));
    }

    @Test public void test_two_logs_in_same_folder() {

        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);
        FileUtils.deleteContents(folder);

        TimestampVariableRollingFileLoggerConfiguration configurationA = new TimestampVariableRollingFileLoggerConfiguration();
        TimestampVariableRollingFileLoggerConfiguration configurationB = new TimestampVariableRollingFileLoggerConfiguration();

        configurationA.setNumberOfFiles(3);
        configurationB.setNumberOfFiles(3);

        configurationA.setNumberOfCompressedFiles(2);
        configurationB.setNumberOfCompressedFiles(2);

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
        TimestampVariableRollingFileLogger loggerA = new TimestampVariableRollingFileLogger();
        TimestampVariableRollingFileLogger loggerB = new TimestampVariableRollingFileLogger();

        FixedTimeProvider time = new FixedTimeProvider(0);
        loggerA.setTimeProvider(time);
        loggerB.setTimeProvider(time);

        loggerA.configure(configurationA, discovery);
        loggerB.configure(configurationB, discovery);

        loggerA.start();
        loggerB.start();

        // Send the first event in
        source.send(eventA1);
        loggerA.flush();

        Map<String, File> files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(1));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));

        // And the second
        source.send(eventB1);
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(2));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000000.log"))[0], is(formatter.format(eventB1)));

        // First roll
        time.increment(TimeUtils.seconds(1));
        source.send(eventA2);
        source.send(eventB2);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(4));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000001.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000001.log"))[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000000.log"))[0], is(formatter.format(eventB1)));

        // Second roll
        time.increment(TimeUtils.seconds(1));
        source.send(eventA3);
        source.send(eventB3);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(6));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000002.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000002.log"))[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000001.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000001.log"))[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000000.log"))[0], is(formatter.format(eventB1)));

        // First zip roll
        time.increment(TimeUtils.seconds(1));
        source.send(eventA4);
        source.send(eventB4);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(8));

        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000003.log"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000003.log"))[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000002.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000002.log"))[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000001.log"))[0], is(formatter.format(eventA2)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000001.log"))[0], is(formatter.format(eventB2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000000.log.zip"), "logA.1970_01_01_000000.log")[0],
                   is(formatter.format(eventA1)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000000.log.zip"), "logB.1970_01_01_000000.log")[0],
                   is(formatter.format(eventB1)));

        // Second zip roll
        time.increment(TimeUtils.seconds(1));
        source.send(eventA5);
        source.send(eventB5);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));

        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000004.log"))[0], is(formatter.format(eventA5)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000004.log"))[0], is(formatter.format(eventB5)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000003.log"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000003.log"))[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000002.log"))[0], is(formatter.format(eventA3)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000002.log"))[0], is(formatter.format(eventB3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000001.log.zip"), "logA.1970_01_01_000001.log")[0],
                   is(formatter.format(eventA2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000001.log.zip"), "logB.1970_01_01_000001.log")[0],
                   is(formatter.format(eventB2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000000.log.zip"), "logA.1970_01_01_000000.log")[0],
                   is(formatter.format(eventA1)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000000.log.zip"), "logB.1970_01_01_000000.log")[0],
                   is(formatter.format(eventB1)));

        // First delete
        time.increment(TimeUtils.seconds(1));
        source.send(eventA6);
        source.send(eventB6);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000005.log"))[0], is(formatter.format(eventA6)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000005.log"))[0], is(formatter.format(eventB6)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000004.log"))[0], is(formatter.format(eventA5)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000004.log"))[0], is(formatter.format(eventB5)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000003.log"))[0], is(formatter.format(eventA4)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000003.log"))[0], is(formatter.format(eventB4)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000002.log.zip"), "logA.1970_01_01_000002.log")[0],
                   is(formatter.format(eventA3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000002.log.zip"), "logB.1970_01_01_000002.log")[0],
                   is(formatter.format(eventB3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000001.log.zip"), "logA.1970_01_01_000001.log")[0],
                   is(formatter.format(eventA2)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000001.log.zip"), "logB.1970_01_01_000001.log")[0],
                   is(formatter.format(eventB2)));

        // Secoond delete
        time.increment(TimeUtils.seconds(1));
        source.send(eventA7);
        source.send(eventB7);
        loggerA.flush();
        loggerB.flush();

        files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(10));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000006.log"))[0], is(formatter.format(eventA7)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000006.log"))[0], is(formatter.format(eventB7)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000005.log"))[0], is(formatter.format(eventA6)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000005.log"))[0], is(formatter.format(eventB6)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000004.log"))[0], is(formatter.format(eventA5)));
        assertThat(FileUtils.readAsStringArray(files.get("logB.1970_01_01_000004.log"))[0], is(formatter.format(eventB5)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000003.log.zip"), "logA.1970_01_01_000003.log")[0],
                   is(formatter.format(eventA4)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000003.log.zip"), "logB.1970_01_01_000003.log")[0],
                   is(formatter.format(eventB4)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logA.1970_01_01_000002.log.zip"), "logA.1970_01_01_000002.log")[0],
                   is(formatter.format(eventA3)));
        assertThat(FileUtils.readZipAsStringArray(files.get("logB.1970_01_01_000002.log.zip"), "logB.1970_01_01_000002.log")[0],
                   is(formatter.format(eventB3)));

        loggerA.close();
        loggerB.close();
    }

    @Test public void test_multiple_channels() {
        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        TimestampVariableRollingFileLoggerConfiguration configurationA = new TimestampVariableRollingFileLoggerConfiguration();

        configurationA.setFolder(folder.getAbsolutePath());

        configurationA.setFilename("logA");

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
        TimestampVariableRollingFileLogger loggerA = new TimestampVariableRollingFileLogger();

        FixedTimeProvider time = new FixedTimeProvider(0);
        loggerA.setTimeProvider(time);
        loggerA.configure(configurationA, discovery);
        loggerA.start();

        // Send the first event in
        source.send(eventA1);
        loggerA.flush();

        Map<String, File> files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(1));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));

        // And the seconds
        source.send(eventB1);
        loggerA.flush();

        assertThat(files.size(), is(1));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[1], is(formatter.format(eventB1)));

        loggerA.close();
    }

    @Test public void test_multiple_channels_no_queue() {
        File folder = FileUtils.createRandomTestFolderForClass(TestRollingFileLogger.class);

        TimestampVariableRollingFileLoggerConfiguration configurationA = new TimestampVariableRollingFileLoggerConfiguration();

        configurationA.setWriteAsynchronously(false);
        configurationA.setFolder(folder.getAbsolutePath());

        configurationA.setFilename("logA");

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
        TimestampVariableRollingFileLogger loggerA = new TimestampVariableRollingFileLogger();

        FixedTimeProvider time = new FixedTimeProvider(0);
        loggerA.setTimeProvider(time);
        loggerA.configure(configurationA, discovery);

        // Send the first event in
        source.send(eventA1);
        loggerA.flush();

        Map<String, File> files = FileUtils.listFilesAsMap(folder);
        assertThat(files.size(), is(1));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));

        // And the seconds
        source.send(eventB1);
        loggerA.flush();

        assertThat(files.size(), is(1));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[0], is(formatter.format(eventA1)));
        assertThat(FileUtils.readAsStringArray(files.get("logA.1970_01_01_000000.log"))[1], is(formatter.format(eventB1)));

        loggerA.close();
    }

}
