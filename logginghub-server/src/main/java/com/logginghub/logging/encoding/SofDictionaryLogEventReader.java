package com.logginghub.logging.encoding;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.InputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DictionaryLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;

public class SofDictionaryLogEventReader implements LogEventReader {

    private File dictionaryFile;

    public SofDictionaryLogEventReader(File dictionaryFile) {
        this.dictionaryFile = dictionaryFile;
    }

    @Override public void readAll(InputStream stream, Destination<LogEvent> destination) {

        Dictionary dictionary = Dictionary.readFrom(dictionaryFile);

        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DictionaryLogEvent.class, 1);

        StreamReaderAbstraction reader = new StreamReaderAbstraction(new BufferedInputStream(stream), Integer.MAX_VALUE);

        try {
            while (true) {
                DictionaryLogEvent event = SofSerialiser.read(reader, configuration);
                DefaultLogEvent logEvent = event.toLogEvent(dictionary);
                destination.send(logEvent);
            }
        }
        catch(EOFException eof){
            // Fine
        }
        catch (SofException e) {
                e.printStackTrace();
        }

    }

}
