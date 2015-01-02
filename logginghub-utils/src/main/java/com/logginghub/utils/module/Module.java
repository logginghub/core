package com.logginghub.utils.module;

import com.logginghub.utils.Asynchronous;

public interface Module<T> extends Asynchronous {
    void configure(T configuration, ServiceDiscovery discovery);
}
