package com.logginghub.logging.encoding;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.FileUtils;

public class JavaSerialisationLogEventWriter implements LogEventWriter {

    private ObjectOutputStream objectOutputStream;

    public JavaSerialisationLogEventWriter(OutputStream outputStream) {
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        try {
            objectOutputStream = new ObjectOutputStream(bos);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void write(LogEvent event) {
        try {
            objectOutputStream.writeUnshared(event);
            objectOutputStream.reset();
            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void close() {
        FileUtils.closeQuietly(objectOutputStream);
    }

    @Override public byte[] encode(DefaultLogEvent event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaSerialisationLogEventWriter writer = new JavaSerialisationLogEventWriter(baos);
        writer.write(event);;
        writer.close();
        return baos.toByteArray();         
    }


}
