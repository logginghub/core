package com.logginghub.logging.servers;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.LogEventInputStream;
import com.logginghub.utils.WorkerThread;

public class PublisherHandlerWorkerThread extends WorkerThread implements LogEventSource
{
    private Socket m_socket;
    private LogEventInputStream m_logEventInputStream;

    private List<LogEventListener> m_listeners = new ArrayList<LogEventListener>();
    private Logger m_logger = Logger.getLogger(this.getClass().getName());

    public PublisherHandlerWorkerThread(Socket socket) throws IOException
    {
        super("PublisherHandlerWorkerThread");
        
        m_socket = socket;
        m_logEventInputStream = new LogEventInputStream(socket.getInputStream());
    }

    public void addLogEventListener(LogEventListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeLogEventListener(LogEventListener listener)
    {
        m_listeners.remove(listener);
    }

    // //////////////////////////////////////////////////////////////////
    // Worker thread methods
    // //////////////////////////////////////////////////////////////////

    @Override
    protected void onRun()
    {
        try
        {
            m_logger.log(Level.FINER,
                         "Trying to read log event from input stream from socket [" + m_socket +
                                 "], this will block...");
            LogEvent logEvent = m_logEventInputStream.readLogEvent();
            m_logger.log(Level.FINER, "Event read : " + logEvent + " from " + m_socket);
            fireListeners(logEvent);
        }
        catch (IOException e)
        {
            m_logger.log(Level.INFO, "Failed to read log event from connection " + m_socket, e);
            stop();
        }
    }

    @Override
    protected void beforeStart()
    {
        m_logger.log(Level.FINE, "Publisher handler thread starting against connection " + m_socket);

    }

    @Override
    protected void beforeStop()
    {
        try
        {
            m_socket.close();
        }
        catch (IOException e)
        {
            m_logger.log(Level.FINE,
                         "Failed to close socket whilst stopping, this is odd but shouldn't cause an issue",
                         e);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private void fireListeners(LogEvent logEvent)
    {
        for(LogEventListener logEventListener : m_listeners)
        {
            logEventListener.onNewLogEvent(logEvent);
        }
    }
}
