package com.logginghub.messaging.netty;

import com.logginghub.messaging.RequestNotification;

public interface Level2Messaging {
    <T> RequestNotification<T> sendRequest(String remoteNode, String remoteChannel, String localNode, String localChannel, Object payload, Class<T> responseClass);
    void addLocalChannelListener(String localNode, String localChannel, WrappedMessageListener wrappedMessageListener);
    void removeLocalChannelListener(String localNode, String localChannel, WrappedMessageListener wrappedMessageListener);
}
