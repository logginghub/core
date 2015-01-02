package com.logginghub.utils;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Concrete implementation of the ListBackedMap using an array list and a hashmap.
 */
public class ArrayListBackedHashMap<K, V> extends ListBackedFactoryMap<K, V> {
    public ArrayListBackedHashMap() {
        super(new ArrayList<V>(), new HashMap<K, V>());
    }

    @Override protected V createEmptyValue(K key) {
        return null;
    }
}
