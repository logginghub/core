package com.logginghub.logging.modules.web;

import com.logginghub.logging.datafiles.SummaryTimeElement;
import com.logginghub.utils.MutableLongValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by james on 01/10/15.
 */
public class SummaryStatistics {

    private long time;
    private long intervalLength;

    private long count;

    private Map<Integer, Long> patternCounts = new HashMap<Integer, Long>();

    public SummaryStatistics(long time, long intervalLength) {
        this.time = time;
        this.intervalLength = intervalLength;
    }

    public Map<Integer, Long> getPatternCounts() {
        return patternCounts;
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public long getTime() {
        return time;
    }

    public void updateFrom(SummaryTimeElement summaryTimeElement) {

        long total = 0;

        Set<Entry<Integer, MutableLongValue<Integer>>> entries = summaryTimeElement.getCountsByPatternId().entrySet();
        for (Entry<Integer, MutableLongValue<Integer>> entry : entries) {
            long value = entry.getValue().value;
            total += value;


            Integer patternId = entry.getKey();
            Long currentCount = patternCounts.get(patternId);
            if(currentCount == null) {
                currentCount = Long.valueOf(0);
            }

            long newValue = currentCount + value;
            patternCounts.put(patternId, newValue);

        }

        count += total;

    }

    public long getCount() {
        return count;
    }
}
