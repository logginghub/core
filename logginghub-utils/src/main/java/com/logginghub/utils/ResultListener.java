package com.logginghub.utils;

public interface ResultListener<T> {
    void onSuccessful(T result);
    void onUnsuccessful(String reason);
    void onFailed(Throwable t);
    void onTimedout();
}
