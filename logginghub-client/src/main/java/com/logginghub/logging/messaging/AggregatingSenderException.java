package com.logginghub.logging.messaging;

import com.logginghub.logging.messages.LoggingMessageCollectionMessage;

public class AggregatingSenderException extends Exception
{
    private static final long serialVersionUID = 1L;
    private LoggingMessageCollectionMessage m_messageCollection = new LoggingMessageCollectionMessage();
    
    public AggregatingSenderException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public void setMessageCollection(LoggingMessageCollectionMessage message)
    {
        m_messageCollection = message;
    }
        
    public LoggingMessageCollectionMessage getMessageCollection()
    {
        return m_messageCollection;
    }
}
