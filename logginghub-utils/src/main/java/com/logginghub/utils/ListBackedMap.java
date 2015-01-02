package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * A map decorator that puts items into a list if they were null keys
 */
public class ListBackedMap<K, V> implements Map<K, V> {

    private final List<V> list;
    private final Map<K, V> map;

    public ListBackedMap(List<V> list, Map<K, V> map) {
        this.list = list;
        this.map = map;
    }

    /**
     * Creates a flat copy of the ListBackedMap passed in, using an ArrayList and
     * HashMap to store the data. 
     * 
     * @param original
     */
    public ListBackedMap(ListBackedMap<K, V> original) {
        this.list = new ArrayList<V>(original.getList());
        this.map = new HashMap<K, V>(original.getMap());
    }

    public List<V> getList() {
        return list;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public int size() {
        return map.size();

    }

    public boolean isEmpty() {
        return map.isEmpty();

    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);

    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);

    }

    public V get(Object key) {
        return map.get(key);

    }

    public V put(K key, V value) {
        
        if(map.containsKey(key)){
            list.remove(map.get(key));
        }
        
        list.add(value);
        
        return map.put(key, value);

    }

    public V remove(Object key) {
        V value = get(key);
        if (value != null) {
            list.remove(value);
        }
        return map.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        Set<? extends K> keySet = m.keySet();
        for (K k : keySet) {
            put(k, m.get(k));
        }
    }

    public void clear() {
        map.clear();
        list.clear();
    }

    public Set<K> keySet() {
        return map.keySet();

    }

    public Collection<V> values() {
        return map.values();

    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }
    
    @Override public String toString() {
        return map.toString();         
    }


}
