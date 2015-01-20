package com.logginghub.logging.messages;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.utils.sof.SerialisableObject;

public class KryoSerialisationStrategy implements SerialisationStrategy {

    private Output outputBuffer = new Output(100000);
    private Kryo kryo = new Kryo();

    public KryoSerialisationStrategy() {
        kryo.register(DefaultLogEvent.class);
        kryo.register(String[].class);
    }
    
    public Kryo getKryo() {
        return kryo;
    }

    public void serialise(ByteBuffer buffer, SerialisableObject event) throws IOException {
        kryo.writeObject(outputBuffer, event);
        buffer.put(outputBuffer.getBuffer(), 0, outputBuffer.position());
        outputBuffer.clear();
    }

    public SerialisableObject deserialise(ByteBuffer buffer) throws IOException {
        Input kryoBuffer = new Input(buffer.array(), buffer.position(), buffer.limit());
        DefaultLogEvent readObject = kryo.readObject(kryoBuffer, DefaultLogEvent.class);
        buffer.position(kryoBuffer.position());
        return readObject;

    }

    public String toString() {
        return "KryoSerialisationStrategy";
    }
}
