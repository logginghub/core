package com.logginghub.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class FactoryMapDecorator<K, V> implements Map<K, V> {

    private final Map<K, V> map;

    public FactoryMapDecorator(Map<K, V> map) {
        this.map = map;
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
    
    public String toString() {
        return map.toString();
    }

    @SuppressWarnings("unchecked") public V get(Object key) {
        V value;
        synchronized (map) {
            value = map.get(key);
            if (value == null) {
                value = createNewValue((K)key);
                put((K) key, value);
            }
        }
        return value;
    }
    
    @SuppressWarnings("unchecked") public V getOnlyIfExists(K key) {
        V value = map.get(key);
        return value;
    }
    
    protected abstract V createNewValue(K key);

    public V put(K key, V value) {
        return map.put(key, value);

    }

    public V remove(Object key) {
        V value = get(key);
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

}
