package com.logginghub.utils.sof.fixtures;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class SimpleStringArrayObject implements SerialisableObject {

    private int value;
    private String[] stringArray;
    private String string;

    public SimpleStringArrayObject() {}

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void read(SofReader reader) throws SofException {
        this.value = reader.readInt(1);
        this.stringArray = reader.readStringArray(2);
        this.string = reader.readString(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
        writer.write(2, stringArray);
        writer.write(3, string);
    }

    public String[] getStringArray() {
        return stringArray;
    }
    
    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
