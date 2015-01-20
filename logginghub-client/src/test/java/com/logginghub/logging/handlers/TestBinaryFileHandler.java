package com.logginghub.logging.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogRecord;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogRecordFactory;
import com.logginghub.logging.handlers.BinaryFileHandler;
import com.logginghub.logging.messaging.LogEventInputStream;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.logging.LogEventComparer;
@RunWith(CustomRunner.class)
public class TestBinaryFileHandler
{
    @Test
    public void testHandler() throws IOException
    {
        BinaryFileHandler handler = new BinaryFileHandler();

        String filename = "target/test.log.binary";
        handler.setOutputFilename(filename);

        LogRecord record1 = LogRecordFactory.getLogRecord1();
        LogRecord record2 = LogRecordFactory.getLogRecord2();
        LogRecord record3 = LogRecordFactory.getLogRecord3();
        LogRecord recordMassive = LogRecordFactory.getLogRecordBig();

        handler.publish(record1);
        handler.publish(record2);
        handler.publish(record3);
        handler.publish(recordMassive);

        handler.close();

        File file = new File(filename);

        LogEventInputStream stream = new LogEventInputStream(new FileInputStream(file));

        LogEvent readLogEvent1 = stream.readLogEvent();
        LogEvent readLogEvent2 = stream.readLogEvent();
        LogEvent readLogEvent3 = stream.readLogEvent();
        LogEvent readLogEvent4 = stream.readLogEvent();

        LogEventComparer.assertEquals(record1, readLogEvent1);
        LogEventComparer.assertEquals(record2, readLogEvent2);
        LogEventComparer.assertEquals(record3, readLogEvent3);
        LogEventComparer.assertEquals(recordMassive, readLogEvent4);

        stream.close();
    }
   
}
