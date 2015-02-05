package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class BaseRequestResponseMessage implements RequestResponseMessage, SerialisableObject {
    private int correlationID;

    public BaseRequestResponseMessage() {
    }

    public int getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(int correlationID) {
        this.correlationID = correlationID;
    }

    @Override public void read(SofReader reader) throws SofException {
        this.setCorrelationID(reader.readInt(0));
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, getCorrelationID());
    }
}
