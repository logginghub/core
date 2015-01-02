package com.logginghub.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapOfLists<K, V>
{
    private Map<K, List<V>> m_map = new HashMap<K, List<V>>(); 
    
    public void add(K key, V value)
    {
        List<V> list = m_map.get(key);
        if(list == null)
        {
            list = new ArrayList<V>();
            m_map.put(key, list);
        }
        
        list.add(value);
    }
    
    public List<V> get(K key)
    {
        return m_map.get(key);
    }

}
