package com.logginghub.logging.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of log events, used by the aggregating senders to 
 * batch together a stack of events.
 * @author admin
 */
public class LoggingMessageCollectionMessage implements LoggingMessage
{
    private List<LoggingMessage> m_messageCollection = new ArrayList<LoggingMessage>();
    public LoggingMessageCollectionMessage()
    {
    }

    public List<LoggingMessage> getMessages()
    {
        return m_messageCollection;
    }
    
    public void addMessage(LoggingMessage message)
    {
        m_messageCollection.add(message);
    }
    
    @Override
    public String toString()
    {
        return "[LoggingMessageCollection messages=" + m_messageCollection.size() + "]";
    }
}
