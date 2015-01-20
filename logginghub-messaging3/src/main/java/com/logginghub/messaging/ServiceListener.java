package com.logginghub.messaging;

public interface ServiceListener<T> {
    void onServiceAvailable(T t);
    void onServiceTimeout();
    void onServiceFailure(Throwable reason);
}
