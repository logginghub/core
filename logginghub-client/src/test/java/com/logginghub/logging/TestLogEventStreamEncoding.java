package com.logginghub.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.messaging.LogEventInputStream;
import com.logginghub.logging.messaging.LogEventOutputStream;
import com.logginghub.testutils.CustomRunner;

@RunWith(CustomRunner.class) public class TestLogEventStreamEncoding {

    @Test public void testStreamSingle() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LogEventOutputStream outputStream = new LogEventOutputStream(baos);

        LogEvent logEvent = LogEventFactory.createFullLogEvent1("testApp");
        outputStream.write(logEvent);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        LogEventInputStream input = new LogEventInputStream(bais);
        LogEvent readLogEvent = input.readLogEvent();

        LogEventComparer.assertEquals(logEvent, readLogEvent);
    }

    @Test public void testStreamSingleMassive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LogEventOutputStream outputStream = new LogEventOutputStream(baos);

        LogEvent logEvent = LogEventFactory.createFullLogEventBig("testApp");
        outputStream.write(logEvent);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        LogEventInputStream input = new LogEventInputStream(bais);
        LogEvent readLogEvent = input.readLogEvent();

        LogEventComparer.assertEquals(logEvent, readLogEvent);
    }

    @Test public void testStreamMultiple() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LogEventOutputStream outputStream = new LogEventOutputStream(baos);

        LogEvent logEvent1 = LogEventFactory.createFullLogEvent1("testApp");
        LogEvent logEvent2 = LogEventFactory.createFullLogEvent2("testApp");
        LogEvent logEvent3 = LogEventFactory.createFullLogEvent3("testApp");

        outputStream.write(logEvent1);
        outputStream.write(logEvent2);
        outputStream.write(logEvent3);

        byte[] byteArray = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        LogEventInputStream input = new LogEventInputStream(bais);

        LogEvent readLogEvent1 = input.readLogEvent();
        LogEvent readLogEvent2 = input.readLogEvent();
        LogEvent readLogEvent3 = input.readLogEvent();

        LogEventComparer.assertEquals(logEvent1, readLogEvent1);
        LogEventComparer.assertEquals(logEvent2, readLogEvent2);
        LogEventComparer.assertEquals(logEvent3, readLogEvent3);
    }

    @Test public void testStreamMultipleWithPause() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LogEventOutputStream outputStream = new LogEventOutputStream(baos);

        LogEvent logEvent1 = LogEventFactory.createFullLogEvent1("testApp");
        LogEvent logEvent2 = LogEventFactory.createFullLogEvent2("testApp");
        LogEvent logEvent3 = LogEventFactory.createFullLogEvent3("testApp");

        outputStream.write(logEvent1);
        outputStream.write(logEvent2);
        outputStream.write(logEvent3);

        byte[] byteArray = baos.toByteArray();

        int chunkSize = 10;
        byte[] firstChunk = new byte[byteArray.length - chunkSize];
        byte[] lastChunk = new byte[chunkSize];

        System.arraycopy(byteArray, 0, firstChunk, 0, firstChunk.length);
        System.arraycopy(byteArray, firstChunk.length, lastChunk, 0, lastChunk.length);

        CrazyDelayedInputStream cdis = new CrazyDelayedInputStream(firstChunk, lastChunk, 2000);
        LogEventInputStream input = new LogEventInputStream(cdis);

        LogEvent readLogEvent1 = input.readLogEvent();
        LogEvent readLogEvent2 = input.readLogEvent();
        LogEvent readLogEvent3 = input.readLogEvent();

        LogEventComparer.assertEquals(logEvent1, readLogEvent1);
        LogEventComparer.assertEquals(logEvent2, readLogEvent2);
        LogEventComparer.assertEquals(logEvent3, readLogEvent3);
    }
}
