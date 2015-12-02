package com.logginghub.logging.datafiles.aggregation;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Out;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 17/09/15.
 */
public class PatternAggregation implements Destination<PatternisedLogEvent>, SerialisableObject {

    private int patternId;
    private long intervalLength;
    private ValueStripper2 stripper;

    private Map<Long, TimeAggregation> dataByTime = new HashMap<Long, TimeAggregation>();

    public PatternAggregation(int patternId, long intervalLength, ValueStripper2 stripper) {
        this.patternId = patternId;
        this.intervalLength = intervalLength;
        this.stripper = stripper;
    }

    public PatternAggregation() {
    }

    public void dump() {

        Out.out("Pattern Id : {}", patternId);

        List<TimeAggregation> ordered = getSortedTimeAggregations();

        for (TimeAggregation timeAggregation : ordered) {
            timeAggregation.dump();
        }
    }

    public void extract() {

    }

    public List<TimeAggregation> getSortedTimeAggregations() {
        List<TimeAggregation> ordered = new ArrayList<TimeAggregation>(dataByTime.values());
        Collections.sort(ordered, new Comparator<TimeAggregation>() {
            @Override
            public int compare(TimeAggregation o1, TimeAggregation o2) {
                return Long.compare(o1.time, o2.time);
            }
        });
        return ordered;
    }

    public int getPatternId() {
        return patternId;
    }

    @Override
    public void read(SofReader reader) throws SofException {

        // The value stripper isn't persisted, and will need to be re-provided.
        this.patternId = reader.readInt(0);
        this.intervalLength = reader.readLong(1);

        dataByTime.clear();
        Collection<TimeAggregation> values = reader.readObjectCollection(2, TimeAggregation.class);
        for (TimeAggregation value : values) {
            dataByTime.put(value.getTime(), value);
        }
    }

    @Override
    public void write(SofWriter writer) throws SofException {
        writer.write(0, patternId);
        writer.write(1, intervalLength);
        writer.write(2, dataByTime.values(), TimeAggregation.class);
    }

    public void send(EventContext eventContext) {
        long time = eventContext.getTime();
        long chunk = TimeUtils.chunk(time, intervalLength);

        TimeAggregation timeAggregation = dataByTime.get(chunk);
        if (timeAggregation == null) {
            timeAggregation = new TimeAggregation(chunk, stripper);
            dataByTime.put(chunk, timeAggregation);
        }

        timeAggregation.send(eventContext);
    }


    @Override
    public void send(PatternisedLogEvent patternisedLogEvent) {
        long time = patternisedLogEvent.getTime();
        long chunk = TimeUtils.chunk(time, intervalLength);

        TimeAggregation timeAggregation = dataByTime.get(chunk);
        if (timeAggregation == null) {
            timeAggregation = new TimeAggregation(chunk, stripper);
            dataByTime.put(chunk, timeAggregation);
        }

        timeAggregation.send(patternisedLogEvent);
    }


}
