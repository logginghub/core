package com.logginghub.logging.servers;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.LogEventOutputStream;
import com.logginghub.logging.utils.LoggingUtils;

public class SocketIOReceiverHandler implements LogEventListener
{
    private LogEventSource m_source;
    private LogEventOutputStream m_logEventOutputStream;
    private Logger m_logger = Logger.getLogger(this.getClass().getName());
    private Socket m_socket;
    
    public SocketIOReceiverHandler(Socket socket, LogEventSource source)
    {
        m_source = source;
        m_socket = socket;

        try
        {
            m_logEventOutputStream = new LogEventOutputStream(socket.getOutputStream());
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Failed to wrap socket output stream with a LogEventOutputStream",
                                       ioe);
        }

        m_source.addLogEventListener(this);
    }

    public void onNewLogEvent(LogEvent event) 
    {
        try
        {
            m_logEventOutputStream.write(event);
        }
        catch (IOException ioe)
        {
            m_logger.log(Level.FINE, "Write to receiver " + m_socket + " failed, closing and removing from event source", ioe);
            LoggingUtils.close(m_socket);
                         
            m_source.removeLogEventListener(this);
        }
    }
}
