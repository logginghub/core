package com.logginghub.utils;

import com.logginghub.utils.module.Module;

public interface NamedModule<T> extends Module<T> {
    String getName();
}
