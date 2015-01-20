package com.logginghub.messaging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.messaging.netty.Level2Messaging;
import com.logginghub.messaging.netty.WrappedMessageListener;

public class MessagingChannel {

    private Level2Messaging messaging;
    private String localChannel;
    private String localNode;
    private String remoteChannel;
    private String remoteNode;
    private List<WrappedMessageListener> ourListeners = new CopyOnWriteArrayList<WrappedMessageListener>();

    public void setSender(Level2Messaging messaging) {
        this.messaging = messaging;
    }

    public void setLocalChannel(String localChannel) {
        this.localChannel = localChannel;
    }

    public void setRemoteChannel(String remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public void setLocalNode(String homeChannel) {
        this.localNode = homeChannel;
    }

    public void setRemoteNodeChannel(String remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public void setRemoteNode(String remoteNode) {
        this.remoteNode = remoteNode;
    }

    public String getLocalChannel() {
        return localChannel;
    }

    public String getLocalNode() {
        return localNode;
    }


    public String getNextDynamicChannel() {
        return localChannel;
    }

    public String getRemoteChannel() {
        return remoteChannel;
    }

    public String getRemoteNode() {
        return remoteNode;
    }

    public <T> RequestNotification<T> sendRequest(Object payload, Class<T> responseClass) {
        return messaging.sendRequest(remoteNode, remoteChannel, localNode, localChannel, payload, responseClass);
    }

    public void addMessageListener(WrappedMessageListener wrappedMessageListener) {
        ourListeners.add(wrappedMessageListener);
        messaging.addLocalChannelListener(localNode, localChannel, wrappedMessageListener);
    }

    public void removeMessageListener(WrappedMessageListener wrappedMessageListener) {
        ourListeners.remove(wrappedMessageListener);
        messaging.removeLocalChannelListener(localNode, localChannel, wrappedMessageListener);
    }

    public void close() {
        for (WrappedMessageListener wrappedMessageListener : ourListeners) {
            messaging.removeLocalChannelListener(localNode, localChannel, wrappedMessageListener);
        }
    }

}
