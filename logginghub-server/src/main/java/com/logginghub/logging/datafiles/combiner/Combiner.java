package com.logginghub.logging.datafiles.combiner;

import com.logginghub.logging.datafiles.aggregation.TimeAggregation;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Out;
import com.logginghub.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 17/09/15.
 */
public class Combiner implements Destination<List<TimeAggregation>> {

    private int patternId;
    private long intervalLength;

    private Map<Long, TimeCombiner> dataByTime = new HashMap<Long, TimeCombiner>();

    public Combiner(int patternId, long interval) {
        this.intervalLength = interval;
        this.patternId = patternId;
    }

    public void send(List<TimeAggregation> sortedTimeAggregations) {

        for (TimeAggregation aggregation : sortedTimeAggregations) {

            long time = aggregation.getTime();
            long chunk = TimeUtils.chunk(time, intervalLength);

            TimeCombiner timeCombiner = dataByTime.get(chunk);
            if (timeCombiner == null) {
                timeCombiner = new TimeCombiner(chunk, intervalLength);
                dataByTime.put(chunk, timeCombiner);
            }

            timeCombiner.send(aggregation);

        }


    }

    public void dump() {

        Out.out("Pattern Id : {}", patternId);

        List<TimeCombiner> ordered = getSortedTimeAggregations();

        for (TimeCombiner timeAggregation : ordered) {
            timeAggregation.dump();
        }
    }

    public List<TimeCombiner> getSortedTimeAggregations() {
        List<TimeCombiner> ordered = new ArrayList<TimeCombiner>(dataByTime.values());
        Collections.sort(ordered, new Comparator<TimeCombiner>() {
            @Override
            public int compare(TimeCombiner o1, TimeCombiner o2) {
                return Long.compare(o1.getTime(), o2.getTime());
            }
        });
        return ordered;
    }

}
