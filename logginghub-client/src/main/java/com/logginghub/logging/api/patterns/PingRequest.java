package com.logginghub.logging.api.patterns;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class PingRequest implements SerialisableObject {

    private long timestamp;
    
    public PingRequest() {
        timestamp = System.currentTimeMillis();
    }
    
    @Override public void read(SofReader reader) throws SofException {
        timestamp = reader.readLong(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(1, timestamp);
    }
    
    @Override public String toString() {
        return super.toString();        
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
