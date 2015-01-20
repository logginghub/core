package com.logginghub.logging.encoding;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DictionaryLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamWriterAbstraction;

public class SofDictionaryLogEventWriter implements LogEventWriter {

    private OutputStream outputStream;
    private SofConfiguration configuration = new SofConfiguration();
    private StreamWriterAbstraction streamWriterAbstraction;

    private Dictionary dictionary = new Dictionary();
    private File dictionaryFile;

    public SofDictionaryLogEventWriter(OutputStream outputStream, File dictionaryFile) {

        this.dictionaryFile = dictionaryFile;
        this.outputStream = new BufferedOutputStream(outputStream, 10 * 1024 * 1024);
        streamWriterAbstraction = new StreamWriterAbstraction(this.outputStream);
        configuration.registerType(DictionaryLogEvent.class, 1);
    }

    @Override public void write(LogEvent event) {

        DictionaryLogEvent dictionaryLogEvent = DictionaryLogEvent.fromLogEvent(event, dictionary);

        try {
            SofSerialiser.write(streamWriterAbstraction, dictionaryLogEvent, configuration);
        }
        catch (SofException e) {
            e.printStackTrace();
        }
    }

    @Override public void close() {
        FileUtils.closeQuietly(outputStream);
        dictionary.writeTo(dictionaryFile);
    }

    @Override public byte[] encode(DefaultLogEvent event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SofDictionaryLogEventWriter writer = new SofDictionaryLogEventWriter(baos, new File("dictionary.dat"));
        writer.write(event);
        writer.close();
        return baos.toByteArray();
    }
}
