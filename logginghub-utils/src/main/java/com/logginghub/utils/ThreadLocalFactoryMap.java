package com.logginghub.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Similar to the {@link FactoryMap} and {@link FactoryMapDecorator}, this is a
 * thread local variation. It uses HashMaps internally, and uses an interface
 * for the factory method rather than being an abstract class. 
 * 
 * @author James
 * 
 * @param <K>
 * @param <V>
 */
public class ThreadLocalFactoryMap<K, V> implements Map<K, V> {

    private ThreadLocal<Map<K, V>> threadLocalMaps = new ThreadLocal<Map<K, V>>() {
        protected java.util.Map<K, V> initialValue() {
            return new HashMap<K, V>();
        }
    };
    private KeyedFactory<K, V> factory;

    public ThreadLocalFactoryMap(KeyedFactory<K, V> factory) {
        this.factory = factory;
    }

    public Map<K, V> getMap() {
        return threadLocalMaps.get();
    }

    public int size() {
        return getMap().size();

    }

    public boolean isEmpty() {
        return getMap().isEmpty();

    }

    public boolean containsKey(Object key) {
        return getMap().containsKey(key);

    }

    public boolean containsValue(Object value) {
        return getMap().containsValue(value);
    }

    public String toString() {
        return threadLocalMaps.toString();
    }

    @SuppressWarnings("unchecked") public V get(Object key) {
        V value;
        Map<K, V> map = getMap();
        value = map.get(key);
        if (value == null) {
            value = factory.create((K) key);
            map.put((K)key, value);
        }
        return value;
    }

    @SuppressWarnings("unchecked") public V getOnlyIfExists(K key) {
        V value = getMap().get(key);
        return value;
    }

    // protected abstract V createNewValue(K key);

    public V put(K key, V value) {
        return getMap().put(key, value);

    }

    public V remove(Object key) {
        V value = get(key);
        return getMap().remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        Set<? extends K> keySet = m.keySet();
        for (K k : keySet) {
            put(k, m.get(k));
        }
    }

    public void clear() {
        getMap().clear();
    }

    public Set<K> keySet() {
        return getMap().keySet();

    }

    public Collection<V> values() {
        return getMap().values();

    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return getMap().entrySet();
    }

}
