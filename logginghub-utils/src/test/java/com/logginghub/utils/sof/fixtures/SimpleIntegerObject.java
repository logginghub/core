package com.logginghub.utils.sof.fixtures;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;


public class SimpleIntegerObject implements SerialisableObject{

    private Integer intType;
    
    public SimpleIntegerObject() {}
    
    public SimpleIntegerObject(int value) {
        this.intType = value;
    }

    public Integer getIntType() {
        return intType;
    }
    
    public void setIntType(Integer intType) {
        this.intType = intType;
    }
    
    public void read(SofReader reader) throws SofException {
        this.intType = reader.readIntObject(4);
    }

    public void write(SofWriter writer) throws SofException {

        writer.write(4, intType);
        
    }
}
