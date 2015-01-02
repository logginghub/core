package com.logginghub.utils;

import java.nio.ByteBuffer;

public interface Encoder<T> {
    void encode(T t, ByteBuffer buffer);
}
