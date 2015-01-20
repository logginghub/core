package com.logginghub.analytics.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.MutableLongValue;

/**
 * Counts events up into buckets. Events are keyed by a string, and the counter
 * is an long.
 * 
 * Note this method is NOT THREAD SAFE. You have to worry about that if you plan
 * to interact with it from multiple threads.
 * 
 * @author James
 * 
 */
public class LongFrequencyCount {

    private Map<String, MutableLongValue> data = new FactoryMapDecorator<String, MutableLongValue>(new ConcurrentHashMap<String, MutableLongValue>()) {
        @Override protected MutableLongValue createNewValue(String key) {
            return new MutableLongValue(key, 0);
        }
    };

    private Metadata metadata = new Metadata();

    private long total;

    public LongFrequencyCount() {}

    public LongFrequencyCount(List<MutableLongValue> data) {
        for (MutableLongValue MutableLongValue : data) {
            count(MutableLongValue.key, MutableLongValue.value);
        }
    }

    public int size() {
        return data.size();
    }
    
    public void count(String seriesKey, long increment) {
        data.get(seriesKey).increment(increment);
        total += increment;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Map<String, MutableLongValue> getData() {
        return data;
    }

    public LongFrequencyCount top(int i) {        
        Collection<MutableLongValue> values = data.values();
        List<MutableLongValue> greatestOf = CollectionUtils.toReverseSortedList(values, i);
        LongFrequencyCount fc = new LongFrequencyCount(greatestOf);
        return fc;
    }
    
    public LongFrequencyCount topWithOthersTotal(int top) {
        Collection<MutableLongValue> values = data.values();
        List<MutableLongValue> greatestOf = CollectionUtils.toReverseSortedList(values);
        
        MutableLongValue other = new MutableLongValue("Other", 0);
        LongFrequencyCount fc = new LongFrequencyCount();
        for(int i = 0; i < greatestOf.size(); i++) {
            MutableLongValue value = greatestOf.get(i);
            if(i < top) { 
                fc.data.put(value.key, value);
            }else{
                other.value += value.value;
            }
        }
        
        fc.data.put(other.key, other);
        
        return fc;
    }
    
    /**
     * @return Returns the list of values in order of highest value first
     */
    public List<MutableLongValue> getSortedValues() {
        List<MutableLongValue> sorted = CollectionUtils.toReverseSortedList(data.values());
        return sorted;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public long getTotal() {
        return total;
    }

    
}
