package com.logginghub.utils.sof.fixtures;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;


public class SimpleStringObject implements SerialisableObject{

    private String value;
    
    public SimpleStringObject() {}
    
    public SimpleStringObject(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public void read(SofReader reader) throws SofException {
        this.value = reader.readString(1);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
    }
}
