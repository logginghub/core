package com.logginghub.utils;

import java.nio.ByteBuffer;

public interface Decoder<T> {
    void decode(T t, ByteBuffer buffer);
}
