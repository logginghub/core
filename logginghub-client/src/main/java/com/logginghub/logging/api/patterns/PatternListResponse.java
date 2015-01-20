package com.logginghub.logging.api.patterns;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class PatternListResponse implements SerialisableObject {

    private String unsuccessfulReason = null;

    private List<Pattern> patterns = new ArrayList<Pattern>();

    public boolean wasSuccessful() {
        return unsuccessfulReason == null;
    }
    
    public void setUnsuccessfulReason(String unsuccessfulReason) {
        this.unsuccessfulReason = unsuccessfulReason;
    }
    
    public String getUnsuccessfulReason() {
        return unsuccessfulReason;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override public void read(SofReader reader) throws SofException {
        unsuccessfulReason = reader.readString(0);
        
        SerialisableObject[] readObjectArray = reader.readObjectArray(1);
        for (SerialisableObject serialisableObject : readObjectArray) {
            patterns.add((Pattern) serialisableObject);
        }
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, unsuccessfulReason);
        writer.write(1, (SerialisableObject[]) patterns.toArray(new SerialisableObject[] {}));
    }
}
