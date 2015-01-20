package com.logginghub.logging.modules.history;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DictionaryLogEvent;
import com.logginghub.logging.messages.SerialisationStrategy;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.sof.ByteBufferReaderAbstraction;
import com.logginghub.utils.sof.ByteBufferSofSerialiser;
import com.logginghub.utils.sof.ByteBufferWriterAbstraction;
import com.logginghub.utils.sof.DefaultSofReader;
import com.logginghub.utils.sof.DefaultSofWriter;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofRuntimeException;
import com.logginghub.utils.sof.SofSerialiser;

public class DictionarySerialisationStrategy implements SerialisationStrategy {

    private SofConfiguration configuration = new SofConfiguration();
    private boolean encodeHeader;
    private boolean microEncoding;

    private Dictionary dictionary = new Dictionary();
    
    // jshaw - optimisations so we dont have to create new abstractions each time
    private ByteBufferWriterAbstraction current = null;
    private ByteBuffer lastBuffer = null;
    private DefaultSofWriter currentWriter = null;

    private ByteBufferSofSerialiser serialiser2 = null;

    public DictionarySerialisationStrategy(boolean encodeHeader, boolean microEncoding) {
        this.encodeHeader = encodeHeader;
        this.microEncoding = microEncoding;
        configuration.registerType(DictionaryLogEvent.class, 0);
        configuration.setMicroFormat(microEncoding);
    }

    @Override public String toString() {
        return "DictionarySerialisationStrategy [encodeHeader=" + encodeHeader + ", microEncoding=" + microEncoding + "]";
    }

    @Override public void serialise(ByteBuffer buffer, SerialisableObject event) throws IOException {

        DictionaryLogEvent dictionaryLogEvent = DictionaryLogEvent.fromLogEvent((DefaultLogEvent)event, dictionary);
        
        SerialisableObject target = dictionaryLogEvent;
        
        if (current == null || buffer != lastBuffer) {
            current = new ByteBufferWriterAbstraction(buffer);
            lastBuffer = buffer;
            currentWriter = new DefaultSofWriter(current, configuration);
            serialiser2 = new ByteBufferSofSerialiser(current, configuration);
        }

        try {
            if (encodeHeader) {
                serialiser2.write(0, target);
            }
            else {
                target.write(currentWriter);
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }
    }

    @Override public SerialisableObject deserialise(ByteBuffer buffer) throws IOException {
        ByteBufferReaderAbstraction bufferAbstraction = new ByteBufferReaderAbstraction(buffer);
        DictionaryLogEvent dictionaryEvent;

        try {
            if (encodeHeader) {
                dictionaryEvent = SofSerialiser.read(bufferAbstraction, configuration);
            }
            else {
                dictionaryEvent = new DictionaryLogEvent();
                DefaultSofReader reader = new DefaultSofReader(bufferAbstraction, configuration);
                dictionaryEvent.read(reader);
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }

        DefaultLogEvent logEvent = dictionaryEvent.toLogEvent(dictionary);
        return logEvent;

    }

}
