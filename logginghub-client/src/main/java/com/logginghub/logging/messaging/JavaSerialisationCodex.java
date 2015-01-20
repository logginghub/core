package com.logginghub.logging.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.ExpandingByteBuffer;

/**
 * Codex that deals with java serialisation, works for any message.
 * @author admin
 *
 */
public class JavaSerialisationCodex
{
    public static void encode(ExpandingByteBuffer expandingByteBuffer, LoggingMessage message)
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(message);
            out.close();

            byte[] buf = bos.toByteArray();
            expandingByteBuffer.putInt(buf.length);
            expandingByteBuffer.put(buf);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to encode message using java serialisation", e);
        }
    }

    public static LoggingMessage decode(ByteBuffer byteBuffer)
    {
        LoggingMessage message = null;
        try
        {
            int size = byteBuffer.getInt();
            byte[] data = new byte[size];
            byteBuffer.get(data);

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            message = (LoggingMessage) in.readObject();
            in.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to decode message using java serialisation", e);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Failed to decode message using java serialisation", e);
        }
        
        return message;
    }
}
