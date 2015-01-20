package com.logginghub.logging.repository;

import com.esotericsoftware.kryo.Kryo;
import com.logginghub.logging.DefaultLogEvent;

public class KryoWrapper extends Kryo {
    public KryoWrapper() {
        register(DefaultLogEvent.class);
        register(String[].class);
    }
}
