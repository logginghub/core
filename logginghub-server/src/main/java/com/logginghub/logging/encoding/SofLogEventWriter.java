package com.logginghub.logging.encoding;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.DefaultSofWriter;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.StreamWriterAbstraction;

public class SofLogEventWriter implements LogEventWriter {

    private OutputStream outputStream;
    private SofConfiguration configuration = new SofConfiguration();
    private StreamWriterAbstraction streamWriterAbstraction;

    public SofLogEventWriter(OutputStream outputStream, boolean micro) {
        this.outputStream = new BufferedOutputStream(outputStream, 10 * 1024 * 1024);
        streamWriterAbstraction = new StreamWriterAbstraction(this.outputStream);
        configuration.registerType(DefaultLogEvent.class, 1);
        configuration.setMicroFormat(micro);
    }

    @Override public void write(LogEvent event) {
        try {
            SerialisableObject so = (SerialisableObject) event;
            so.write(new DefaultSofWriter(streamWriterAbstraction, configuration));
        }
        catch (SofException e) {
            e.printStackTrace();
        }
    }

    @Override public void close() {
        FileUtils.closeQuietly(outputStream);
    }

    @Override public byte[] encode(DefaultLogEvent event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SofLogEventWriter writer = new SofLogEventWriter(baos, false);
        writer.write(event);
        writer.close();
        return baos.toByteArray();
    }

}
