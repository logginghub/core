package com.logginghub.messaging;

public interface ResponseListener<T> {
    void onResponse(T response);
    void onResponseFailure(Throwable reason);
    void onResponseTimeout();
}
