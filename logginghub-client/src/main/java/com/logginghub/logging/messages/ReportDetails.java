package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 04/02/15.
 */
public class ReportDetails implements SerialisableObject {

    private String name;
    private String command;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override public void read(SofReader reader) throws SofException {
        name = reader.readString(0);
        command = reader.readString(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, name);
        writer.write(1, command);
    }
}
