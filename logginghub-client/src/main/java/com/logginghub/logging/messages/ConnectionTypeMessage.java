package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ConnectionTypeMessage extends BaseRequestResponseMessage implements SerialisableObject {

    private int type;
    private String name;

    public ConnectionTypeMessage(int type) {
        super();
        this.type = type;
    }

    public ConnectionTypeMessage() {
    }

    public ConnectionTypeMessage(String name, int connectionTypeHubBridge) {
        this.name = name;
        this.type = connectionTypeHubBridge;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void read(SofReader reader) throws SofException {
        this.type = reader.readInt(1);
        this.name = reader.readString(2);
        this.setCorrelationID(reader.readInt(3));
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, type);
        writer.write(2, name);
        writer.write(3,getCorrelationID());
    }

}
