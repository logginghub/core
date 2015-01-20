package com.logginghub.logging.messages;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class LogEventMessage implements LoggingMessage, SerialisableObject {
    private LogEvent logEvent;

    public LogEventMessage(LogEvent event) {
        logEvent = event;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    @Override public String toString() {
        return "[LogEventMessage message='" + logEvent.getMessage() + "']";
    }

    public void read(SofReader reader) throws SofException {
        this.logEvent = (DefaultLogEvent)reader.readObject(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, (DefaultLogEvent)logEvent);
    }

}
