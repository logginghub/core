package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.ExpandingByteBuffer;

/**
 * @deprecated you should be using the LoggingMessage streams instead these days.
 * @author admin
 */
public class LogEventOutputStream extends OutputStream
{
    private OutputStream m_decorated;
    private ExpandingByteBuffer m_expandingByteBuffer = new ExpandingByteBuffer(0);

    public LogEventOutputStream(OutputStream decorated)
    {
        m_decorated = decorated;
    }

    public void write(LogEvent event) throws IOException
    {
        LogEventCodex.encode(m_expandingByteBuffer, event);

        ByteBuffer buffer = m_expandingByteBuffer.getBuffer();
        buffer.flip();

        while(buffer.hasRemaining())
        {
            write(buffer.get());
        }
        
        buffer.compact();
    }

    @Override
    public void write(int b) throws IOException
    {
        m_decorated.write(b);
    }

    @Override
    public void close() throws IOException
    {     
        super.close();
        m_decorated.close();
    }
}
