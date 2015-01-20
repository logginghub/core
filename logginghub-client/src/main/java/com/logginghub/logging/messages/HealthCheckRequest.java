package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HealthCheckRequest implements SerialisableObject {

    private String type;

    public HealthCheckRequest() {}

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void read(SofReader reader) throws SofException {
        this.type = reader.readString(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, type);
    }

}
