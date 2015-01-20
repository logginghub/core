package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.utils.ExpandingByteBuffer;

/**
 * InputStream for LogEvents. It isn't really an InputStream though, just
 * follows the InputStream decorator pattern approach.
 * 
 * @author admin
 */
public class LogEventInputStream
{
    private InputStream m_decorated;
    private ExpandingByteBuffer m_expandingByteBuffer = new ExpandingByteBuffer();

    public LogEventInputStream(InputStream decorated)
    {
        m_decorated = decorated;
    }

    public void close() throws IOException
    {
        m_decorated.close();
    }

    public LogEvent readLogEvent() throws IOException
    {
        byte[] inputBuffer = new byte[4096];

        LogEvent event = null;
        boolean needsMoreBytes = false;

        boolean eof = false;

        // This gets a little complicated as there might already be a complete
        // or partial event on the bytebuffer from the last read attempt. Check
        // that first.
        while(event == null && !eof)
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
                    // End of file
                    eof = true;                  
                }
            }

            if(!eof)
            {
                // Flip into read mode
                buffer.flip();

                try
                {
                    event = LogEventCodex.decode(buffer);
                }
                catch (PartialMessageException e)
                {
                    needsMoreBytes = true;
                }

                // Back into receive mode
                buffer.compact();
            }
        }

        return event;
    }
}
