package com.logginghub.utils.sof;

public interface SerialisableObject {
    void read(SofReader reader) throws SofException;

    void write(SofWriter writer) throws SofException;
}
