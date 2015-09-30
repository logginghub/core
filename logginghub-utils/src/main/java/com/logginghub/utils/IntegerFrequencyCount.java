package com.logginghub.utils;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Counts events up into buckets. Events are keyed by a string, and the counter is an integer.
 * <p/>
 * Note this method is NOT THREAD SAFE. You have to worry about that if you plan to interact with it from multiple threads.
 *
 * @author James
 */
public class IntegerFrequencyCount implements SerialisableObject {

    @SuppressWarnings("serial")
    private FactoryMap<String, MutableIntegerValue> data = new FactoryMap<String, MutableIntegerValue>() {
        @Override
        protected MutableIntegerValue createEmptyValue(String key) {
            return new MutableIntegerValue(key, 0);
        }
    };

    private Metadata metadata = new Metadata();

    private int total;

    public IntegerFrequencyCount() {
    }

    public IntegerFrequencyCount(List<MutableIntegerValue> data) {
        for (MutableIntegerValue mutableIntegerValue : data) {
            count(mutableIntegerValue.key, mutableIntegerValue.value);
        }
    }

    public void count(Object seriesKey, int increment) {
        data.get(seriesKey).increment(increment);
        total += increment;
    }

    public void count(IntegerFrequencyCount counts) {
        Map<String, MutableIntegerValue> data = counts.getData();
        for (Entry<String, MutableIntegerValue> entry : data.entrySet()) {
            String key = entry.getKey();
            MutableIntegerValue value = entry.getValue();
            int count = value.value;

            count(key, count);
        }
    }

    public Map<String, MutableIntegerValue> getData() {
        return data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * @return Returns the list of values in order of highest value first
     */
    public List<MutableIntegerValue> getSortedValues() {
        return CollectionUtils.toReverseSortedList(data.values());
    }

    public int getTotal() {
        return total;
    }

    @Override
    public void read(SofReader reader) throws SofException {
        data.clear();
        int field = 0;
        this.total = reader.readInt(field++);
        int length = reader.readInt(field++);
        for (int i = 0; i < length; i++) {
            String key = reader.readString(field++);
            int count = reader.readInt(field++);
            data.get(key).increment(count);
        }
    }

    @Override
    public void write(SofWriter writer) throws SofException {
        int field = 0;
        writer.write(field++, total);
        writer.write(field++, data.size());
        for (Entry<String, MutableIntegerValue> entry : data.entrySet()) {
            writer.write(field++, entry.getKey());
            writer.write(field++, entry.getValue().value);
        }
    }

    public IntegerFrequencyCount top(int top) {
        List<MutableIntegerValue> topList = CollectionUtils.toReverseSortedList(data.values(), top);
        IntegerFrequencyCount fc = new IntegerFrequencyCount(topList);
        return fc;
    }

}
