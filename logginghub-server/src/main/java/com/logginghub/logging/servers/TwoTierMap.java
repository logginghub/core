package com.logginghub.logging.servers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwoTierMap<A, B, C> {

    Map<A, Map<B, C>> data = new HashMap<A, Map<B, C>>();

    public List<C> getAll(A key) {
        List<C> list = new ArrayList<C>();
        synchronized (data) {
            Map<B, C> map = data.get(key);
            if (map != null) {
                list.addAll(map.values());
            }
        }
        return list;
    }

    public Map<B, C> getOrCreate(A key) {
        Map<B, C> map;
        synchronized (data) {
            map = data.get(key);
            if (map == null) {
                map = new HashMap<B, C>();
                data.put(key, map);
            }
        }

        return map;
    }

    public C put(A a, B b, C c) {
        C oldC;
        synchronized (data) {
            Map<B, C> map = getOrCreate(a);
            oldC = map.put(b, c);
        }

        return oldC;
    }

    public C get(A a, B b) {
        C c;
        synchronized (data) {
            Map<B, C> map = data.get(a);
            if (map == null) {
                c = null;
            }
            else {
                c = map.get(b);
            }
        }

        return c;
    }

    public C remove(A a, B b) {
        C c;
        synchronized (data) {
            Map<B, C> map = data.get(a);
            if (map == null) {
                c = null;
            }
            else {
                c = map.remove(b);
                if (map.isEmpty()) {
                    data.remove(a);
                }
            }
        }

        return c;
    }

    public boolean contains(A a, B b) {
        boolean contains;
        
        synchronized (data) {
            Map<B, C> map = data.get(a);
            if (map == null) {
                contains = false;
            }
            else {
                contains = map.containsKey(b);
            }

        }

        return contains;

    }

}
