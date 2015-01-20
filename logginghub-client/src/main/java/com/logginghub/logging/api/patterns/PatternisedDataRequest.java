package com.logginghub.logging.api.patterns;

import com.logginghub.logging.api.patterns.PatternisedDataAPI.SerialisableAggreatedEventFilter;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class PatternisedDataRequest implements SerialisableObject {

    private int patternID;
    private long from;
    private long to;
    
    private SerialisableAggreatedEventFilter filter;
    
    @Override public void read(SofReader reader) throws SofException {
        this.patternID = reader.readInt(0);
        this.from = reader.readLong(1);
        this.to = reader.readLong(2);
        this.filter = (SerialisableAggreatedEventFilter) reader.readObject(3);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, patternID);
        writer.write(1, from);
        writer.write(2, to);
        writer.write(3, filter);
    }
    
}
