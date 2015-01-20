package com.logginghub.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.AbstractLogEventSource;
import com.logginghub.logging.messaging.LogEventInputStream;

/**
 * LogEventSource for log events stored in a binary file.
 * 
 * @author admin
 */
public class FileSource extends AbstractLogEventSource
{
    private File m_file;
    private LogEventInputStream m_inputStream;

    public FileSource(File file)
    {
        m_file = file;

    }

    public boolean loadNextEvent() throws IOException
    {
        ensureOpen();
        LogEvent readLogEvent = m_inputStream.readLogEvent();
        
        boolean more;
        
        if(readLogEvent != null)
        {
            fireNewLogEvent(readLogEvent);
            more = true;
        }
        else
        {
            reset();
            more = false;
        }
        
        return more;
    }

    private void ensureOpen() throws IOException
    {
        if(m_inputStream == null)
        {
            m_inputStream = new LogEventInputStream(new FileInputStream(m_file));
        }
    }

    public void reset() throws IOException
    {
        if(m_inputStream != null)
        {
            m_inputStream.close();
            m_inputStream = null;
        }
    }

    /**
     * Read the events from the file and send them to any attached
     * LogEventListeners
     */
    public void loadAllRemainingEvents() throws IOException
    {
        ensureOpen();
        LogEvent readLogEvent = null;
        while((readLogEvent = m_inputStream.readLogEvent()) != null)
        {
            fireNewLogEvent(readLogEvent);
        }

        reset();
    }

    public File getFile()
    {     
        return m_file;
    }
}
