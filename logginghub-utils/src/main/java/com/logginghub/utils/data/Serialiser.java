package com.logginghub.utils.data;

public interface Serialiser<T> {
    void serialise(T t, SerialisationWriter writer);
    T deserialise(SerialisationReader reader);
}
