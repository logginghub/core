package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ClearEventsMessage implements SerialisableObject, LoggingMessage {


    public ClearEventsMessage() {

    }

    public void read(SofReader reader) throws SofException {

    }

    public void write(SofWriter writer) throws SofException {

    }

    @Override public String toString() {
        return "ClearEventsMessage";
    }
    
    
    
}
