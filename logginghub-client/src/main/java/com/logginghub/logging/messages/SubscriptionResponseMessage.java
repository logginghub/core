package com.logginghub.logging.messages;

import java.io.Serializable;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class SubscriptionResponseMessage implements LoggingMessage, Serializable, SerialisableObject {
    public SubscriptionResponseMessage() {}

    @Override public String toString() {
        return "[SubscriptionResponseMessage]";
    }

    public void read(SofReader reader) throws SofException {}

    public void write(SofWriter writer) throws SofException {}
}
