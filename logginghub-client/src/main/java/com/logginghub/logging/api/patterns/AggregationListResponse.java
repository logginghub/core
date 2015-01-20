package com.logginghub.logging.api.patterns;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class AggregationListResponse implements SerialisableObject {

    private String unsuccessfulReason = null;

    private List<Aggregation> aggregations = new ArrayList<Aggregation>();

    public boolean wasSuccessful() {
        return unsuccessfulReason == null;
    }
    
    public void setUnsuccessfulReason(String unsuccessfulReason) {
        this.unsuccessfulReason = unsuccessfulReason;
    }
    
    public String getUnsuccessfulReason() {
        return unsuccessfulReason;
    }

    public List<Aggregation> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<Aggregation> patterns) {
        this.aggregations = patterns;
    }

    @Override public void read(SofReader reader) throws SofException {
        unsuccessfulReason = reader.readString(0);
        
        SerialisableObject[] readObjectArray = reader.readObjectArray(1);
        for (SerialisableObject serialisableObject : readObjectArray) {
            aggregations.add((Aggregation) serialisableObject);
        }
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, unsuccessfulReason);
        writer.write(1, (SerialisableObject[]) aggregations.toArray(new SerialisableObject[] {}));
    }
}
