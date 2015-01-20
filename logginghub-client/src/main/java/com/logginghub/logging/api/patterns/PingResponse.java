package com.logginghub.logging.api.patterns;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class PingResponse implements SerialisableObject {

    private InstanceDetails instanceDetails = new InstanceDetails();
    private long timestamp;

    @Override public void read(SofReader reader) throws SofException {
        instanceDetails = (InstanceDetails)reader.readObject(0);
        timestamp = reader.readLong(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, instanceDetails);
        writer.write(1, timestamp);
    }
    
    public InstanceDetails getInstanceDetails() {
        return instanceDetails;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setInstanceDetails(InstanceDetails instanceDetails) {
        this.instanceDetails = instanceDetails;
    }
}

