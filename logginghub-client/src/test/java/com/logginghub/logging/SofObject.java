package com.logginghub.logging;

import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class SofObject implements SerialisableObject, LoggingMessage {

    private String message;
    
    public SofObject() {}

    public SofObject(String message) {
        this.message = message;
    }

    public void read(SofReader reader) throws SofException {
        message = reader.readString(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, message);
    }

    public String getMessage() {
        return message;
    }
}
