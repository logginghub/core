package com.logginghub.logging.messages;

import java.io.Serializable;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class UnsubscriptionRequestMessage implements LoggingMessage, Serializable, SerialisableObject {
    private static final long serialVersionUID = 1L;

    public UnsubscriptionRequestMessage() {}

    @Override public String toString() {
        return "[UnsubscriptionRequestMessage]";
    }

    public void read(SofReader reader) throws SofException {}

    public void write(SofWriter writer) throws SofException {}
}
