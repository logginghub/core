package com.logginghub.utils;

import java.util.List;
import java.util.Map;

/*
 * Extension to the ListBackedMap to add FactoryMap behaviour.
 */
public abstract class ListBackedFactoryMap<K, V> extends ListBackedMap<K, V> {

    public ListBackedFactoryMap(List<V> list, Map<K, V> map) {
        super(list, map);
    }

    @Override public V get(Object key) {
        V value = super.get(key);
        if(value == null){
            value = createEmptyValue((K)key);
            put((K)key, value);
        }
        return value;         
    }
    
    
    protected abstract V createEmptyValue(K key);

    
}
