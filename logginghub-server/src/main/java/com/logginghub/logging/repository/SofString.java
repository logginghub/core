package com.logginghub.logging.repository;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 29/01/15.
 */
public class SofString implements SerialisableObject {
    private String value;

    public SofString() {
    }

    public SofString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override public void read(SofReader reader) throws SofException {
        value = reader.readString(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(1, value);
    }
}
