package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.utils.ExpandingByteBuffer;

public class LoggingMessageInputStream extends InputStream
{
    private InputStream m_decorated;
    private ExpandingByteBuffer m_expandingByteBuffer = new ExpandingByteBuffer();
    private LoggingMessageCodex m_codex = new LoggingMessageCodex();

    public LoggingMessageInputStream(InputStream decorated)
    {
        m_decorated = decorated;
    }

    public LoggingMessage readLogEvent() throws IOException
    {
        byte[] inputBuffer = new byte[4096];

        LoggingMessage message = null;
        boolean needsMoreBytes = false;
        boolean eof = false;

        // This gets a little complicated as there might already be a complete
        // or partial event on the bytebuffer from the last read attempt. Check
        // that first.
        while(message == null)
        {
            ByteBuffer buffer = m_expandingByteBuffer.getBuffer();

            if(buffer.position() == 0 || needsMoreBytes)
            {
                int bytesRead = m_decorated.read(inputBuffer);

                if(bytesRead != -1)
                {
                    buffer = m_expandingByteBuffer.getBuffer(bytesRead);
                    buffer.put(inputBuffer, 0, bytesRead);
                }
                else
                {
                    eof = true;
                }
            }

            // Flip into read mode
            buffer.flip();

            int position = 0;
            try
            {
                position = buffer.position();
                message = m_codex.decode(buffer);
            }
            catch (PartialMessageException e)
            {
                buffer.position(position);
                needsMoreBytes = true;
            }

            // Back into receive mode
            buffer.compact();
        }

        return message;
    }

    @Override
    public int read() throws IOException
    {
        return m_decorated.read();
    }

    @Override
    public void close() throws IOException
    {
        super.close();
        m_decorated.close();
    }
}
