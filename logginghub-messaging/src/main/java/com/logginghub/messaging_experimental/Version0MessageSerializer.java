package com.logginghub.messaging_experimental;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import com.logginghub.utils.ArrayUtils;

public class Version0MessageSerializer implements MessageSerializer
{
    private static final int MAX_BUFFER_SIZE = 4 * 1024 * 1024 * 10;

    private byte[] encodeBuffer = new byte[4 * 10 * 1024];

    private int headerSize = 4 /* to */+ 4 /* from */+ 4 + /* correlation */+4 /*
                                                                                * payload
                                                                                * id
                                                                                */;

    public ByteBuffer serialize(Message message)
    {
        ByteBuffer buffer = null;
        boolean ok = false;
        while (!ok)
        {
            try
            {
                buffer = ByteBuffer.wrap(encodeBuffer);

                byte[] payload = message.getPayload();
                int length = payload.length + headerSize;

                buffer.put((byte) 0);
                buffer.putInt(length);
                buffer.putInt(message.getFromID());
                buffer.putInt(message.getToID());
                buffer.putInt(message.getCorrelationID());
                buffer.putInt(message.getPayloadID());
                buffer.put(payload);
            }
            catch (BufferOverflowException overflow)
            {
                if (encodeBuffer.length * 2 > MAX_BUFFER_SIZE)
                {
                    throw new RuntimeException("The write buffer has grown bigger than we allow it, are you trying to send something bonkers?");
                }
                encodeBuffer = ArrayUtils.doubleSize(encodeBuffer);
            }
        }

        return buffer;
    }

    public Message attemptToDecode(ByteBuffer wrapped)
    {
        Message message = null;
        
        // Remember the version number has already been decoded
        if (wrapped.remaining() >= 4)
        {
            int length = wrapped.getInt();
            if (wrapped.remaining() >= length)
            {
                int fromID = wrapped.getInt();
                int toID = wrapped.getInt();
                int correlationID = wrapped.getInt();
                int payloadID = wrapped.getInt();
                int payloadLength = length - headerSize;
                byte[] payload = new byte[payloadLength];
                wrapped.get(payload);

                message = new Message();
                message.setCorrelationID(correlationID);
                message.setFromID(fromID);
                message.setPayload(payload);
                message.setPayloadID(payloadID);
                message.setToID(toID);
            }
        }
        
        return message;
    }
}
