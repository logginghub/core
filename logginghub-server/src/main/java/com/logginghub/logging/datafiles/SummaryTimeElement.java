package com.logginghub.logging.datafiles;

import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.MutableLongValue;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by james on 18/09/15.
 */
public class SummaryTimeElement implements SerialisableObject {

    private long time;
    private long intervalLength;

    private FactoryMap<Integer, MutableLongValue<Integer>> countsByPatternId = new FactoryMap<Integer, MutableLongValue<Integer>>() {
        @Override
        protected MutableLongValue<Integer> createEmptyValue(Integer key) {
            return new MutableLongValue<Integer>(key, 0);
        }
    };

    public SummaryTimeElement(long time, long intervalLength) {
        this.time = time;
        this.intervalLength = intervalLength;
    }

    public SummaryTimeElement() {
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public long getCountForPattern(int patternId) {
        long count;

        MutableLongValue<Integer> onlyIfExists = countsByPatternId.getOnlyIfExists(patternId);
        if(onlyIfExists != null) {
            count = onlyIfExists.value;
        }else{
            count = 0;
        }

        return count;
    }

    public FactoryMap<Integer, MutableLongValue<Integer>> getCountsByPatternId() {
        return countsByPatternId;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        StringUtilsBuilder stringBuilder = new StringUtilsBuilder();
        stringBuilder.appendLine("Time     : {}", Logger.toTimeString(time));
        stringBuilder.appendLine("Interval : {}", TimeUtils.formatIntervalMilliseconds(intervalLength));
        stringBuilder.appendLine();

        List<MutableLongValue<Integer>> sorted =new ArrayList<MutableLongValue<Integer>>(countsByPatternId.values());
        Collections.sort(sorted, new Comparator<MutableLongValue<Integer>>() {
            @Override
            public int compare(MutableLongValue<Integer> o1, MutableLongValue<Integer> o2) {
                return Integer.compare(o1.key, o2.key);
            }
        });

        for (MutableLongValue<Integer> value : sorted) {
            stringBuilder.appendLine("   {} = {}", value.key, value.value);
        }

        return stringBuilder.toString();
    }

    @Override
    public void read(SofReader reader) throws SofException {
        int field = 0;
        this.time = reader.readLong(field++);
        this.intervalLength = reader.readLong(field++);
        int values = reader.readInt(field++);
        countsByPatternId.clear();
        for(int i = 0; i < values; i++) {
            int patternId = reader.readInt(field++);
            long count = reader.readLong(field++);
            countsByPatternId.get(patternId).increment(count);
        }
    }

    @Override
    public void write(SofWriter writer) throws SofException {
        int field = 0;
        writer.write(field++, time);
        writer.write(field++, intervalLength);
        writer.write(field++, countsByPatternId.size());
        for (Entry<Integer, MutableLongValue<Integer>> entry : countsByPatternId.entrySet()) {
            writer.write(field++, (int)entry.getKey());
            writer.write(field++, entry.getValue().value);
        }
    }

    public void update(int patternId, long count) {
        countsByPatternId.get(patternId).increment(count);
    }
}
