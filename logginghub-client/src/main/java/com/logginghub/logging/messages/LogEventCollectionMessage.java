package com.logginghub.logging.messages;

import com.logginghub.logging.LogEventCollection;

public class LogEventCollectionMessage implements LoggingMessage {
    private LogEventCollection m_logEventCollection;

    public LogEventCollectionMessage(LogEventCollection event) {
        m_logEventCollection = event;
    }

    public LogEventCollection getLogEventCollection() {
        return m_logEventCollection;
    }

    @Override public String toString() {
        return "[LogEventCollection size=" + m_logEventCollection.size() + "]";
    }
}
