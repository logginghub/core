package com.logginghub.logging.messages;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.utils.Factory;
import com.logginghub.utils.sof.ByteBufferReaderAbstraction;
import com.logginghub.utils.sof.ByteBufferSofSerialiser;
import com.logginghub.utils.sof.ByteBufferWriterAbstraction;
import com.logginghub.utils.sof.DefaultSofReader;
import com.logginghub.utils.sof.DefaultSofWriter;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofRuntimeException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SofSerialisationStrategy<T extends SerialisableObject> implements SerialisationStrategy {

    private SofConfiguration configuration = new SofConfiguration();
    private boolean encodeHeader;
    private boolean microEncoding;

    // jshaw - optimisations so we dont have to create new abstractions each time
    private ByteBufferWriterAbstraction current = null;
    private ByteBuffer lastBuffer = null;
    private DefaultSofWriter currentWriter = null;

    private ByteBufferSofSerialiser serialiser2 = null;
    private Factory<T> factory;

    public SofSerialisationStrategy(boolean encodeHeader, boolean microEncoding) {
        this.encodeHeader = encodeHeader;
        this.microEncoding = microEncoding;
        // configuration.registerType(DefaultLogEvent.class, 0);
        // configuration.registerType(PatternisedLogEvent.class, 1);
        // configuration.registerType(AggregatedLogEvent.class, 2);
        configuration.setMicroFormat(microEncoding);

        setFactory(new Factory<T>() {
            @Override public T create() {
                return (T) new DefaultLogEvent();
            }
        });
    }

    public void setConfiguration(SofConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setFactory(Factory<T> factory) {
        this.factory = factory;
    }

    @Override public String toString() {
        return "SofSerialisationStrategy [encodeHeader=" + encodeHeader + ", microEncoding=" + microEncoding + "]";
    }

    public void serialise(ByteBuffer buffer, SerialisableObject event) throws IOException {

        if (current == null || buffer != lastBuffer) {
            current = new ByteBufferWriterAbstraction(buffer);
            lastBuffer = buffer;
            currentWriter = new DefaultSofWriter(current, configuration);
            serialiser2 = new ByteBufferSofSerialiser(current, configuration);
        }

        try {
            if (encodeHeader) {
                Integer typeID = configuration.resolve(event.getClass());
                if(typeID == null) {
                    throw new SofException("Type not registered '{}'", event.getClass().getName());
                }
                serialiser2.write(typeID, event);
            }
            else {
                event.write(currentWriter);
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }
    }

    public SerialisableObject deserialise(ByteBuffer buffer) throws IOException {
        ByteBufferReaderAbstraction bufferAbstraction = new ByteBufferReaderAbstraction(buffer);
        T event;

        try {
            if (encodeHeader) {
                event = ByteBufferSofSerialiser.read(bufferAbstraction, configuration);
            }
            else {
                event = factory.create();
                DefaultSofReader reader = new DefaultSofReader(bufferAbstraction, configuration);
                event.read(reader);
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }

        return event;

    }

    public SofConfiguration getConfiguration() {
        return configuration;
    }
}
