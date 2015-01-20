package com.logginghub.messaging2.encoding.encodable;

public interface Encodable<T> {
    void encode(WriteBuffer buffer);
    T decode(ReadBuffer buffer);
}
