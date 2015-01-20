package com.logginghub.logging.modules.history;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.messages.SerialisationStrategy;
import com.logginghub.logging.repository.KryoWrapper;
import com.logginghub.utils.sof.SerialisableObject;

public class KryoSerialisationStrategy implements SerialisationStrategy {

    private KryoWrapper kryoWrapper = new KryoWrapper();
    private Output outputBuffer = new Output(100000);
    
    @Override public void serialise(ByteBuffer buffer, SerialisableObject event) throws IOException {
        kryoWrapper.writeObject(outputBuffer, event);        
        buffer.put(outputBuffer.getBuffer(), 0, outputBuffer.position());
        outputBuffer.clear();
    }

    @Override public SerialisableObject deserialise(ByteBuffer buffer) throws IOException {
        Input kryoBuffer = new Input(buffer.array(), buffer.position(), buffer.limit());
        DefaultLogEvent readObject = kryoWrapper.readObject(kryoBuffer, DefaultLogEvent.class);
        buffer.position(kryoBuffer.position());
        return readObject;

    }

    @Override public String toString() {
        return "KryoSerialisationStrategy";
         
    }
}
