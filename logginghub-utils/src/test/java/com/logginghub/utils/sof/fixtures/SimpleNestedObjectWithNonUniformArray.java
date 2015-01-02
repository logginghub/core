package com.logginghub.utils.sof.fixtures;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class SimpleNestedObjectWithNonUniformArray implements SerialisableObject {

    private int value;

    private SimpleIntObject intObject;
    private SerialisableObject[] objectArray = new SerialisableObject[10];
    private String string;

    public SimpleNestedObjectWithNonUniformArray() {}

    public SimpleNestedObjectWithNonUniformArray(int value) {
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
        this.intObject = (SimpleIntObject) reader.readObject(2);
        this.objectArray = (SerialisableObject[])reader.readObjectArray(3);
        this.string = reader.readString(4);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
        writer.write(2, intObject);
        writer.write(3, objectArray);
        writer.write(4, string);
    }

    public SimpleIntObject getIntObject() {
        return intObject;
    }

    public void setIntObject(SimpleIntObject intObject) {
        this.intObject = intObject;
    }
    
    public String getString() {
        return string;
    }
    
    public void setString(String string) {
        this.string = string;
    }
    
    public SerialisableObject[] getObjectArray() {
        return objectArray;
    }
    
    public void setObjectArray(SerialisableObject[] objectArray) {
        this.objectArray = objectArray;
    }
}
