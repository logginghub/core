package com.logginghub.logging.datafiles.aggregation;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatternCollection;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Is;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by james on 17/09/15.
 */
public class Aggregation implements Destination<PatternisedLogEvent>, SerialisableObject {

    private PatternCollection patternCollection;
    private long intervalLength = 1000;

    private Map<Integer, PatternAggregation> dataByPatternId = new HashMap<Integer, PatternAggregation>();

    public Aggregation(PatternCollection patternCollection, long intervalLength) {
        this.patternCollection = patternCollection;
        this.intervalLength = intervalLength;
    }

    public Aggregation() {
    }

    public void dump() {
        for (Entry<Integer, PatternAggregation> patternData : dataByPatternId.entrySet()) {
            patternData.getValue().dump();
        }
    }

    public Collection<PatternAggregation> getPatternData() {
        return dataByPatternId.values();
    }

    @Override
    public void read(SofReader reader) throws SofException {
        // TODO : the pattern collection must have been provided already, and must match what was stored before!
        dataByPatternId.clear();
        Collection<PatternAggregation> values = reader.readObjectCollection(0, PatternAggregation.class);
        for (PatternAggregation value : values) {
            dataByPatternId.put(value.getPatternId(), value);
        }
    }

    @Override
    public void write(SofWriter writer) throws SofException {
        writer.write(0, dataByPatternId.values(), PatternAggregation.class);
    }

    @Override
    public void send(PatternisedLogEvent patternisedLogEvent) {
        int patternId = patternisedLogEvent.getPatternID();
        PatternAggregation patternAggregation = dataByPatternId.get(patternId);

        if (patternAggregation == null) {
            ValueStripper2 stripper = patternCollection.getStripper(patternId);
            Is.notNull(stripper, "Couldn't find pattern metadata (stripper) for patternId '{}'", patternId);
            patternAggregation = new PatternAggregation(patternId, intervalLength, stripper);
            dataByPatternId.put(patternId, patternAggregation);
        }

        try {
            patternAggregation.send(patternisedLogEvent);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


}
