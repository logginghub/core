package com.logginghub.logging.messages;

import java.io.Serializable;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class FilterRequestMessage implements LoggingMessage, Serializable, SerialisableObject {
    private static final long serialVersionUID = 1L;
    private int levelFilter;

    public FilterRequestMessage() {}
    
    public FilterRequestMessage(int levelFilter) {
        this.levelFilter = levelFilter;
    }
    
    public int getLevelFilter() {
        return levelFilter;
    }

    @Override public String toString() {
        return "FilterRequestMessage [levelFilter=" + levelFilter + "]";
    }

    public void read(SofReader reader) throws SofException {
        levelFilter = reader.readInt(0);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(0, levelFilter);
    }
}
