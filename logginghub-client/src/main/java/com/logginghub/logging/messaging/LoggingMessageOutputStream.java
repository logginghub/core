package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.ExpandingByteBuffer;

/**
 * OutputStream that writes LoggingMessages using the LoggingMessageCodex.
 * 
 * @author admin
 */
public class LoggingMessageOutputStream extends OutputStream
{
    private OutputStream m_decorated;
    private ExpandingByteBuffer m_expandingByteBuffer = new ExpandingByteBuffer(0);
    private EnumSet<LoggingMessageCodex.Flags> m_flags = EnumSet.noneOf(LoggingMessageCodex.Flags.class);
    private LoggingMessageCodex m_codex = new LoggingMessageCodex();

    public LoggingMessageOutputStream(OutputStream decorated)
    {
        m_decorated = decorated;
    }

    public void setFlags(EnumSet<LoggingMessageCodex.Flags> flags)
    {
        m_flags = flags;
    }

    public void write(LogEvent event) throws IOException
    {               
        m_codex.encode(m_expandingByteBuffer, event, m_flags);

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
