package com.logginghub.logging.modules.history;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.messages.SerialisationStrategy;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatterniserModule;
import com.logginghub.logging.modules.configuration.PatterniserConfiguration;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.module.ProxyServiceDiscovery;
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

public class PatternisedSerialisationStrategy implements SerialisationStrategy {

    private SofConfiguration configuration = new SofConfiguration();
    private boolean encodeHeader;
    private boolean microEncoding;

    private Dictionary dictionary = new Dictionary();

    // jshaw - optimisations so we dont have to create new abstractions each time
    private ByteBufferWriterAbstraction current = null;
    private ByteBuffer lastBuffer = null;
    private DefaultSofWriter currentWriter = null;

    private ByteBufferSofSerialiser serialiser2 = null;
    private PatterniserModule patterniser;

    public PatternisedSerialisationStrategy(boolean encodeHeader, boolean microEncoding) {
        this.encodeHeader = encodeHeader;
        this.microEncoding = microEncoding;
        configuration.registerType(PatternisedLogEvent.class, 0);
        configuration.registerType(DefaultLogEvent.class, 1);
        configuration.setMicroFormat(microEncoding);

        patterniser = new PatterniserModule();
        PatterniserConfiguration patterniserConfiguration = JAXBConfiguration.loadConfiguration(PatterniserConfiguration.class, "patterniser.xml");
        patterniser.configure(patterniserConfiguration, new ProxyServiceDiscovery());
    }

    @Override public String toString() {
        return "DictionarySerialisationStrategy [encodeHeader=" + encodeHeader + ", microEncoding=" + microEncoding + "]";
    }

    @Override public void serialise(ByteBuffer buffer, SerialisableObject event) throws IOException {

        if (current == null || buffer != lastBuffer) {
            current = new ByteBufferWriterAbstraction(buffer);
            lastBuffer = buffer;
            currentWriter = new DefaultSofWriter(current, configuration);
            serialiser2 = new ByteBufferSofSerialiser(current, configuration);
        }

        PatternisedLogEvent patternised = patterniser.patternise((DefaultLogEvent)event);

        try {
            if (patternised != null) {
                if (encodeHeader) {
                    serialiser2.write(0, patternised);
                }
                else {
                    // Need a type header
                    current.writeByte((byte)0);
                    patternised.write(currentWriter);
                }
            }
            else {
                if (encodeHeader) {
                    serialiser2.write(1, event);
                }
                else {
                    // Need a type header
                    current.writeByte((byte)1);
                    event.write(currentWriter);
                }

            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }
    }

    @Override public SerialisableObject deserialise(ByteBuffer buffer) throws IOException {
        ByteBufferReaderAbstraction bufferAbstraction = new ByteBufferReaderAbstraction(buffer);
       
        DefaultLogEvent event; 
        try {
            if (encodeHeader) {
                SerialisableObject object = SofSerialiser.read(bufferAbstraction, configuration);
                if(object instanceof PatternisedLogEvent) {
                    PatternisedLogEvent patternisedLogEvent = (PatternisedLogEvent) object;
                    event = patterniser.depatternise(patternisedLogEvent);
                }else{
                    event = (DefaultLogEvent)object;
                }
            }
            else {
                
                DefaultSofReader reader = new DefaultSofReader(bufferAbstraction, configuration);
                byte type = bufferAbstraction.readByte();
                if(type == 0) {
                    PatternisedLogEvent patternisedLogEvent = new PatternisedLogEvent();
                    patternisedLogEvent.read(reader);
                    event = patterniser.depatternise(patternisedLogEvent);
                }else{
                    event = new DefaultLogEvent();
                    event.read(reader);
                }
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }

        return event;

    }

}
