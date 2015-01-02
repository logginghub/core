package com.logginghub.utils.sof.fixtures;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ObjectVersion2 implements SerialisableObject {

    private int value;
    private String string;
    private String string2 = "default";

    public ObjectVersion2() {}

    public ObjectVersion2(int value, String string, String string2) {
        this.value = value;
        this.string = string;
        this.string2 = string2;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void read(SofReader reader) throws SofException {
        this.value = reader.readInt(1);
        this.string = reader.readString(2);
        
        // TODO : decide between these two
//        this.string2 = reader.readString(3, "default");
        
        if (reader.hasMore()) {
            this.string2 = reader.readString(3);
        }
        else {
            this.string2 = "default";
        }
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
        writer.write(2, string);
        writer.write(3, string2);
    }
}
