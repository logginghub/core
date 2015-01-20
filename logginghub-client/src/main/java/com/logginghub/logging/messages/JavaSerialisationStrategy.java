package com.logginghub.logging.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.sof.SerialisableObject;

public class JavaSerialisationStrategy implements SerialisationStrategy {

    public void serialise(ByteBuffer buffer, SerialisableObject logEvent) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(logEvent);
        out.close();

        byte[] buf = bos.toByteArray();
        buffer.putInt(buf.length);
        buffer.put(buf);
    }
    
    

    @Override public String toString() {
        return "JavaSerialisationStrategy ";
    }


    public SerialisableObject deserialise(ByteBuffer byteBuffer) throws IOException {

        int size = byteBuffer.getInt();
        byte[] data = new byte[size];
        byteBuffer.get(data);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        DefaultLogEvent event;
        try {
            event = (DefaultLogEvent) in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new FormattedRuntimeException(e, "Failed to create object");
        }
        in.close();

        return event;
    }


}
