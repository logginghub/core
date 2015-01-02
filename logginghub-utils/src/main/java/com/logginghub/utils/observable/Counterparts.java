package com.logginghub.utils.observable;

import java.util.HashMap;

import com.logginghub.utils.KeyedFactory;

public class Counterparts<T1, T2> extends HashMap<T1, T2>{
    private static final long serialVersionUID = 1L;

    public T2 create(T1 key, KeyedFactory<T1, T2> factory) {
        T2 created = factory.create(key);
        put(key, created);
        return created;
    }

}
