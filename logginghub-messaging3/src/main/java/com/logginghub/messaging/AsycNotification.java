package com.logginghub.messaging;

public interface AsycNotification {
    void onSuccess();
    void onFailure(Throwable reason);
    void onTimeout();
}
