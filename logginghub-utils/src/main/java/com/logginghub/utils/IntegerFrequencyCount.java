package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Counts events up into buckets. Events are keyed by a string, and the counter
 * is an integer.
 * 
 * Note this method is NOT THREAD SAFE. You have to worry about that if you plan
 * to interact with it from multiple threads.
 * 
 * @author James
 * 
 */
public class IntegerFrequencyCount {

    @SuppressWarnings("serial") private FactoryMap<String, MutableIntegerValue> data = new FactoryMap<String, MutableIntegerValue>() {
        @Override protected MutableIntegerValue createEmptyValue(String key) {
            return new MutableIntegerValue(key, 0);
        }
    };

    private Metadata metadata = new Metadata();

    private int total;

    public IntegerFrequencyCount() {}

    public IntegerFrequencyCount(List<MutableIntegerValue> data) {
        for (MutableIntegerValue mutableIntegerValue : data) {
            count(mutableIntegerValue.key, mutableIntegerValue.value);
        }
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void count(String seriesKey, int increment) {
        data.get(seriesKey).increment(increment);
        total += increment;
    }

    public Map<String, MutableIntegerValue> getData() {
        return data;
    }

    public IntegerFrequencyCount top(int top) {
        List<MutableIntegerValue> topList = CollectionUtils.toReverseSortedList(data.values(), top);
        IntegerFrequencyCount fc = new IntegerFrequencyCount(topList);
        return fc;
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

}
