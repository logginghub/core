package com.logginghub.messaging;

public abstract class AsycNotificationAdaptor implements AsycNotification {
    
    public abstract void onSuccess();
    
    public void onFailure(Throwable reason) {
        throw new RuntimeException(reason);
    }
    
    public void onTimeout() {
        throw new RuntimeException("Asynchronous operation timed out");
    }
}
