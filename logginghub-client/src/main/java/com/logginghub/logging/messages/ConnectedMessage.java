package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ConnectedMessage implements SerialisableObject, LoggingMessage {

    private int connectionID;
    
    public ConnectedMessage(int connectionID) {
        this.connectionID = connectionID;
    }

    public ConnectedMessage() {}
    
    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }
    
    public int getConnectionID() {
        return connectionID;
    }

    @Override public void read(SofReader reader) throws SofException {
        this.connectionID = reader.readInt(0);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, connectionID);
    }

    @Override public String toString() {
        return "ConnectedMessage [connectionID=" + connectionID + "]";
    }
    
    
    
}
