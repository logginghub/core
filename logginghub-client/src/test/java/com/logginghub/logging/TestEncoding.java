package com.logginghub.logging;

import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.logging.messaging.LogEventCodex;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.ExpandingByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.logging.LogRecord;

import static org.junit.Assert.*;

@RunWith(CustomRunner.class) public class TestEncoding {

    @Test public void testEncodeDecode() throws PartialMessageException {
        LogEventCodex codex = new LogEventCodex();
        LogRecord record = LogRecordFactory.getLogRecord1();
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(record, "Test Application");

        // ByteBuffer buffer = ByteBuffer.allocate(4096);
        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        LogEventCodex.encode(buffer, event);
        buffer.flip();

        LogEvent fullLogEvent = LogEventCodex.decode(buffer.getBuffer());

        assertEquals(event.getOriginTime(), fullLogEvent.getOriginTime());
        assertEquals(event.getSequenceNumber(), fullLogEvent.getSequenceNumber());
        assertEquals(event.getFormattedException(), fullLogEvent.getFormattedException());
        assertEquals(event.getFormattedObject(), fullLogEvent.getFormattedObject());
        assertEquals(event.getLevel(), fullLogEvent.getLevel());
        assertEquals(event.getJavaLevel(), fullLogEvent.getJavaLevel());
        assertEquals(event.getLoggerName(), fullLogEvent.getLoggerName());
        assertEquals(event.getMessage(), fullLogEvent.getMessage());
        assertEquals(event.getSourceApplication(), fullLogEvent.getSourceApplication());
        assertEquals(event.getSourceClassName(), fullLogEvent.getSourceClassName());
        assertEquals(event.getSourceHost(), fullLogEvent.getSourceHost());
        assertEquals(event.getSourceMethodName(), fullLogEvent.getSourceMethodName());
        assertEquals(event.getThreadName(), fullLogEvent.getThreadName());
    }

    @Test public void test_encode_decode_metadata() throws PartialMessageException {
        LogEventCodex codex = new LogEventCodex();
        LogRecord record = LogRecordFactory.getLogRecord1();
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(record, "Test Application");
        event.getMetadata().put("meta", "data");

        // ByteBuffer buffer = ByteBuffer.allocate(4096);
        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        LogEventCodex.encode(buffer, event);
        buffer.flip();

        LogEvent fullLogEvent = LogEventCodex.decode(buffer.getBuffer());

        assertEquals(event.getOriginTime(), fullLogEvent.getOriginTime());
        assertEquals(event.getSequenceNumber(), fullLogEvent.getSequenceNumber());
        assertEquals(event.getFormattedException(), fullLogEvent.getFormattedException());
        assertEquals(event.getFormattedObject(), fullLogEvent.getFormattedObject());
        assertEquals(event.getLevel(), fullLogEvent.getLevel());
        assertEquals(event.getJavaLevel(), fullLogEvent.getJavaLevel());
        assertEquals(event.getLoggerName(), fullLogEvent.getLoggerName());
        assertEquals(event.getMessage(), fullLogEvent.getMessage());
        assertEquals(event.getSourceApplication(), fullLogEvent.getSourceApplication());
        assertEquals(event.getSourceClassName(), fullLogEvent.getSourceClassName());
        assertEquals(event.getSourceHost(), fullLogEvent.getSourceHost());
        assertEquals(event.getSourceMethodName(), fullLogEvent.getSourceMethodName());
        assertEquals(event.getThreadName(), fullLogEvent.getThreadName());
        assertEquals("data", fullLogEvent.getMetadata().get("meta"));
    }

    @Test public void testPartialPacket() throws PartialMessageException {
        LogEventCodex codex = new LogEventCodex();
        LogEventFactory factory = new LogEventFactory();
        LogRecord record = LogRecordFactory.getLogRecord1();
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(record, "Test Application");

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        codex.encode(buffer, event);
        buffer.flip();

        // Deliberately frig the buffer
        int size = buffer.remaining();
        int friggedSize = size / 2;
        buffer.limit(friggedSize);

        try {
            LogEvent fullLogEvent = LogEventCodex.decode(buffer.getBuffer());
            fail("This decode should have failed");
        }
        catch (PartialMessageException pme) {

        }

        // Make sure its reset the buffer nicely
        assertEquals(0, buffer.position());
        assertEquals(friggedSize, buffer.remaining());

        // Unfrig it
        buffer.limit(size);
        LogEvent fullLogEvent = LogEventCodex.decode(buffer.getBuffer());

        assertEquals(event.getOriginTime(), fullLogEvent.getOriginTime());
        assertEquals(event.getSequenceNumber(), fullLogEvent.getSequenceNumber());
        assertEquals(event.getFormattedException(), fullLogEvent.getFormattedException());
        assertEquals(event.getFormattedObject(), fullLogEvent.getFormattedObject());
        assertEquals(event.getLevel(), fullLogEvent.getLevel());
        assertEquals(event.getJavaLevel(), fullLogEvent.getJavaLevel());
        assertEquals(event.getLoggerName(), fullLogEvent.getLoggerName());
        assertEquals(event.getMessage(), fullLogEvent.getMessage());
        assertEquals(event.getSourceApplication(), fullLogEvent.getSourceApplication());
        assertEquals(event.getSourceClassName(), fullLogEvent.getSourceClassName());
        assertEquals(event.getSourceHost(), fullLogEvent.getSourceHost());
        assertEquals(event.getSourceMethodName(), fullLogEvent.getSourceMethodName());
        assertEquals(event.getThreadName(), fullLogEvent.getThreadName());
    }
}
