package com.logginghub.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConcurrentFactoryMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unchecked") public synchronized V get(Object key) {
        V value = super.get(key);
        if(value == null){
            value = createEmptyValue((K) key);
            super.put((K) key, value);
        }
        return value;
    }

    @SuppressWarnings("unchecked") public V getOnlyIfExists(Object key) {
        V value = super.get(key);
        return value;
    }
    
    protected abstract V createEmptyValue(K key);

    /**
     * @return A plain hashmap containing the data from this factorymap. Useful when we dont want to serialise the factory aspects in kryo!
     */
    public Map<K, V> toHashMap() {
        return new HashMap<K,V>(this);
         
    }

}
