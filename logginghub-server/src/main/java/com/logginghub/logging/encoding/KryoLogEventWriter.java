package com.logginghub.logging.encoding;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.io.Output;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.KryoWrapper;

public class KryoLogEventWriter implements LogEventWriter {

    private KryoWrapper kryoWrapper;
    private Output buffer;

    public KryoLogEventWriter(OutputStream os) {
        kryoWrapper = new KryoWrapper();
        buffer = new Output(os);
    }

    @Override public void write(LogEvent event) {
        kryoWrapper.writeObject(buffer, event);
    }

    @Override public void close() {
        buffer.close();
    }

    @Override public byte[] encode(DefaultLogEvent event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        KryoLogEventWriter writer = new KryoLogEventWriter(baos);
        writer.write(event);;
        writer.close();
        return baos.toByteArray();
    }

}
