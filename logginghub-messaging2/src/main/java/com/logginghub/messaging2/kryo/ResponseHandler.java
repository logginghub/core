package com.logginghub.messaging2.kryo;

public interface ResponseHandler<T> {
    void onResponse(T response);
}
