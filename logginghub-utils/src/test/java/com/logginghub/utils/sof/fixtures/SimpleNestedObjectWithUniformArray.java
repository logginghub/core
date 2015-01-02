package com.logginghub.utils.sof.fixtures;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class SimpleNestedObjectWithUniformArray implements SerialisableObject {

    private int value;
    private SimpleIntObject[] objectArray;
    private String string;

    public SimpleNestedObjectWithUniformArray() {}

    public SimpleNestedObjectWithUniformArray(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void read(SofReader reader) throws SofException {
        this.value = reader.readInt(1);
        this.objectArray = (SimpleIntObject[])reader.readObjectArray(2, SimpleIntObject.class);
        this.string = reader.readString(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
        writer.write(2, objectArray, SimpleIntObject.class);
        writer.write(3, string);
    }

    public SimpleIntObject[] getObjectArray() {
        return objectArray;
    }

    public void setObjectArray(SimpleIntObject[] objectArray) {
        this.objectArray = objectArray;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    
}
