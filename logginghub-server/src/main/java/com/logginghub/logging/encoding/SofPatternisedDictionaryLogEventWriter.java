package com.logginghub.logging.encoding;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DictionaryLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatterniserModule;
import com.logginghub.logging.modules.configuration.PatterniserConfiguration;
import com.logginghub.logging.utils.PatternisedDictionaryEvent;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import com.logginghub.utils.sof.DefaultSofWriter;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.StreamWriterAbstraction;

public class SofPatternisedDictionaryLogEventWriter implements LogEventWriter {

    private OutputStream outputStream;
    private SofConfiguration configuration = new SofConfiguration();
    private StreamWriterAbstraction streamWriterAbstraction;
    private PatterniserModule patterniser;
    private int patternised = 0;
    private int unpatternised = 0;
    private PatternHelper patternHelper = new PatternHelper();
    private Dictionary dictionary = new Dictionary();
    private File dictionaryFile;

    // TODO : load and save dictionary

    public SofPatternisedDictionaryLogEventWriter(OutputStream outputStream, boolean micro, File dictionaryFile) {
        this.dictionaryFile = dictionaryFile;
        this.outputStream = new BufferedOutputStream(outputStream, 10 * 1024 * 1024);
        streamWriterAbstraction = new StreamWriterAbstraction(this.outputStream);
        configuration.registerType(DefaultLogEvent.class, 1);
        configuration.registerType(PatternisedDictionaryEvent.class, 2);
        configuration.setMicroFormat(micro);

        patterniser = new PatterniserModule();
        PatterniserConfiguration patterniserConfiguration = JAXBConfiguration.loadConfiguration(PatterniserConfiguration.class, "patterniser.xml");
        patterniser.configure(patterniserConfiguration, new ProxyServiceDiscovery());
    }

    @Override public void write(LogEvent event) {
        try {
            PatternisedLogEvent patternised = patterniser.patternise(event);
            if (patternised != null) {
                PatternisedDictionaryEvent patternisedDictionaryEvent = new PatternisedDictionaryEvent(patternised.getTime(),
                                                                                                       patternised.getPatternID(),
                                                                                                       dictionary);
                patternisedDictionaryEvent.setVariables(patternised.getVariables());

                streamWriterAbstraction.writeByte((byte) 2);
                patternisedDictionaryEvent.write(new DefaultSofWriter(streamWriterAbstraction, configuration));
                // SofSerialiser.write(streamWriterAbstraction, patternisedDictionaryEvent,
                // configuration);
                this.patternised++;
            }
            else {
                patternHelper.process(event.getMessage());
                // System.out.println(event.getMessage());
                DictionaryLogEvent dictionaryLogEvent = DictionaryLogEvent.fromLogEvent(event, dictionary);
                // SofSerialiser.write(streamWriterAbstraction, event2, configuration);
                streamWriterAbstraction.writeByte((byte) 1);
                dictionaryLogEvent.write(new DefaultSofWriter(streamWriterAbstraction, configuration));

                this.unpatternised++;
            }
        }
        catch (SofException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPatternised() {
        return patternised;
    }

    public int getUnpatternised() {
        return unpatternised;
    }

    @Override public void close() {
        FileUtils.closeQuietly(outputStream);
        dictionary.writeTo(dictionaryFile);
    }

    @Override public byte[] encode(DefaultLogEvent event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SofPatternisedDictionaryLogEventWriter writer = new SofPatternisedDictionaryLogEventWriter(baos, true, new File("dictionary.dat"));
        writer.write(event);
        writer.close();
        return baos.toByteArray();
    }
}
