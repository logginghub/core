package com.logginghub.messaging2;

import com.esotericsoftware.kryo.Kryo;

public interface ClassRegisterer {

    void registerClasses(Kryo kryo);

}
